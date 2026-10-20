package br.com.apigestaodeestoque.exception;

public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem); // Repassa a mensagem de erro para a classe mãe do Java
    }
}