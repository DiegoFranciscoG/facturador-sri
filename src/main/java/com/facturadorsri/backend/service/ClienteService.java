package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.Cliente;
import com.facturadorsri.backend.domain.enums.NombreCatalogo;
import com.facturadorsri.backend.domain.enums.TipoIdentificacion;
import com.facturadorsri.backend.dto.cliente.ClienteRequest;
import com.facturadorsri.backend.dto.cliente.ClienteResponse;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.mapper.ClienteMapper;
import com.facturadorsri.backend.repository.ClienteRepository;
import com.facturadorsri.backend.service.sri.CedulaValidator;
import com.facturadorsri.backend.service.sri.RucValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final CatalogoSriService catalogoSriService;
    private final RucValidator rucValidator;
    private final CedulaValidator cedulaValidator;

    public ClienteResponse crear(ClienteRequest request) {
        validarIdentificacion(request.tipoIdentificacion(), request.identificacion());
        clienteRepository.findByTipoIdentificacionAndIdentificacion(
                        request.tipoIdentificacion(), request.identificacion())
                .ifPresent(c -> {
                    throw new BusinessException("Ya existe un cliente con esa identificacion");
                });

        Cliente cliente = clienteMapper.aEntidad(request);
        return clienteMapper.aResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return clienteMapper.aResponse(buscarOLanzar(id));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(clienteMapper::aResponse);
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        validarIdentificacion(request.tipoIdentificacion(), request.identificacion());
        Cliente cliente = buscarOLanzar(id);
        clienteMapper.actualizar(request, cliente);
        return clienteMapper.aResponse(clienteRepository.save(cliente));
    }

    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private Cliente buscarOLanzar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    private void validarIdentificacion(String tipoIdentificacionCodigo, String identificacion) {
        catalogoSriService.validarVigente(NombreCatalogo.TIPO_IDENTIFICACION, tipoIdentificacionCodigo);
        TipoIdentificacion tipo = TipoIdentificacion.deCodigo(tipoIdentificacionCodigo);
        boolean valida = switch (tipo) {
            case RUC -> rucValidator.esValido(identificacion);
            case CEDULA -> cedulaValidator.esValida(identificacion);
            case CONSUMIDOR_FINAL -> identificacion.equals("9999999999999");
            case PASAPORTE, IDENTIFICACION_EXTERIOR -> identificacion.length() >= 5;
        };
        if (!valida) {
            throw new BusinessException("La identificacion no es valida para el tipo declarado");
        }
    }
}
