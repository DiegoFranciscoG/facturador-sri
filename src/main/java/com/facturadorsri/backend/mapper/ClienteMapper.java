package com.facturadorsri.backend.mapper;

import com.facturadorsri.backend.domain.Cliente;
import com.facturadorsri.backend.dto.cliente.ClienteRequest;
import com.facturadorsri.backend.dto.cliente.ClienteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClienteMapper {

    Cliente aEntidad(ClienteRequest request);

    ClienteResponse aResponse(Cliente cliente);

    void actualizar(ClienteRequest request, @MappingTarget Cliente cliente);
}
