package com.jve.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jve.Entity.PedidoProducto;
import java.util.List;

@Repository
public interface PedidoProductoRepository extends JpaRepository<PedidoProducto, Integer> {
    List<PedidoProducto> findByPedidoId(Integer idPedido);
} 