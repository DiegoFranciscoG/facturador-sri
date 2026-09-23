package com.facturadorsri.backend.mapper;

import com.facturadorsri.backend.domain.Producto;
import com.facturadorsri.backend.dto.producto.ProductoRequest;
import com.facturadorsri.backend.dto.producto.ProductoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductoMapper {

    Producto aEntidad(ProductoRequest request);

    ProductoResponse aResponse(Producto producto);

    void actualizar(ProductoRequest request, @MappingTarget Producto producto);
}
