package br.com.apigestaodeestoque.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "quantidade_disponivel")
    private Integer quantidadeDisponivel;

    @Column(name = "quantidade_reservada", nullable = false)
    private Integer quantidadeReservada = 0;

    @Column(nullable = false, name = "quantidade_minima")
    private Integer quantidadeMinima;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false, unique = true)
    private Produto produto;
}