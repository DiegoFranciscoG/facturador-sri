package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.Producto;
import com.facturadorsri.backend.domain.enums.NombreCatalogo;
import com.facturadorsri.backend.dto.producto.ProductoRequest;
import com.facturadorsri.backend.dto.producto.ProductoResponse;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.mapper.ProductoMapper;
import com.facturadorsri.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final CatalogoSriService catalogoSriService;

    public ProductoResponse crear(ProductoRequest request) {
        validarCatalogos(request);
        if (productoRepository.existsByCodigoPrincipal(request.codigoPrincipal())) {
            throw new BusinessException("Ya existe un producto con ese codigo principal");
        }
        Producto producto = productoMapper.aEntidad(request);
        producto.setActivo(true);
        return productoMapper.aResponse(productoRepository.save(producto));
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return productoMapper.aResponse(buscarOLanzar(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(Pageable pageable) {
        return productoRepository.findAll(pageable).map(productoMapper::aResponse);
    }

    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        validarCatalogos(request);
        Producto producto = buscarOLanzar(id);
        productoMapper.actualizar(request, producto);
        return productoMapper.aResponse(productoRepository.save(producto));
    }

    public void desactivar(Long id) {
        Producto producto = buscarOLanzar(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    Producto buscarEntidadOLanzar(Long id) {
        return buscarOLanzar(id);
    }

    private Producto buscarOLanzar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
    }

    private void validarCatalogos(ProductoRequest request) {
        catalogoSriService.validarVigente(NombreCatalogo.CODIGO_IMPUESTO, request.codigoImpuesto());
        catalogoSriService.validarVigente(NombreCatalogo.CODIGO_PORCENTAJE_IVA, request.codigoPorcentajeIva());
    }
}
