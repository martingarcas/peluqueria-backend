package com.jve.Repository;

import com.jve.Entity.PedidoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoProductoRepository extends JpaRepository<PedidoProducto, Integer> {
    List<PedidoProducto> findByPedidoId(Integer pedidoId);
} 