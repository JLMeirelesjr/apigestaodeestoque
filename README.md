# 📦 API de Gestão de Estoque e Pedidos

API REST desenvolvida em **Java 17** e **Spring Boot** para gerenciar estoque e pedidos, com controle de ciclo de vida do pedido, atualização de estoque na mesma transação e histórico de auditoria. Documentação interativa via Swagger UI.

---

## 🚀 Tecnologias Utilizadas

* **Java 17**
* **Spring Boot 3.3.4** <!-- CONFIRMAR a versão no build.gradle -->
  * Spring Web (MVC)
  * Spring Data JPA / Hibernate
  * Spring Security <!-- CONFIRMAR: remova se estiver só como dependência, sem uso real -->
  * Spring Validation
* **PostgreSQL** (via Docker)
* **SpringDoc OpenAPI** (Swagger UI)
* **JUnit 5 + Mockito** <!-- CONFIRMAR: remova se o projeto ainda não tem testes -->
* **Lombok**
* **Gradle**

---

## 🛠️ Arquitetura e Regras de Negócio

O projeto segue arquitetura em camadas (Controller, Service, Repository, Entity, DTO).

1. **Estoque atualizado de forma atômica:** criar ou cancelar um pedido altera o estoque na mesma transação. Se qualquer etapa falhar, nada é gravado, evitando venda sem estoque disponível ou estoque inconsistente após um cancelamento.
2. **Ciclo de vida do pedido:** `AGUARDANDO_PAGAMENTO` ➔ `PAGO` ➔ `ENVIADO` ou `CANCELADO`. <!-- CONFIRMAR: de quais status o cancelamento é permitido. Se possível, troque esta linha por uma tabela "de → para". -->
3. **Histórico de auditoria (`PedidoHistorico`):** cada mudança de status gera um registro de histórico, consultável por endpoint próprio.

---

## 📋 Endpoints

<!-- CONFIRMAR: ajuste esta tabela com as rotas reais do seu Controller -->

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/pedidos` | Cria um pedido e baixa o estoque |
| `GET` | `/api/pedidos/{id}` | Busca um pedido pelo ID |
| `PATCH` | `/api/pedidos/{id}/status` | Atualiza o status do pedido |
| `GET` | `/api/pedidos/{id}/historico` | Lista o histórico de transições |

**Exemplo de requisição** (`POST /api/pedidos`):
```json
{
  "usuarioId": 1,
  "itens": [
    { "produtoId": 1, "quantidade": 2 }
  ]
}
```

> Para essa requisição funcionar, já precisam existir no banco um usuário e um produto com os IDs informados.

---

## 📋 Pré-requisitos

* **JDK 17** ou superior
* **Docker**
* **Git**

---

## ⚙️ Como Executar o Projeto

### 1. Clonar o repositório
```bash
git clone https://github.com/JLMeirelesjr/apigestaodeestoque.git
cd apigestaodeestoque
```

### 2. Subir o banco de dados
<!-- CONFIRMAR: nome do banco, usuário e senha usados no seu projeto. Se existir docker-compose.yml, use "docker compose up -d" no lugar do comando abaixo. -->
```bash
docker run --name postgres-estoque \
  -e POSTGRES_DB=db_estoque \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

### 3. Configurar as variáveis de ambiente
<!-- CONFIRMAR: os nomes devem ser iguais aos do application.properties. Remova esta seção se o projeto não usa variáveis de ambiente. -->

No PowerShell:
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/db_estoque"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
```

No Linux/Mac:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/db_estoque
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

### 4. Rodar a aplicação
```bash
./gradlew bootRun
```
No Windows:
```bash
gradlew.bat bootRun
```

A aplicação sobe em `http://localhost:8090`. <!-- CONFIRMAR a porta (server.port no application.properties) -->

### 5. Acessar a documentação (Swagger)
```
http://localhost:8090/swagger-ui.html
```

### 6. Rodar os testes
<!-- CONFIRMAR: remova esta seção se não houver testes -->
```bash
./gradlew test
```

---

## 👤 Autor

**José Lúcio Meireles Júnior**

* GitHub: [@JLMeirelesjr](https://github.com/JLMeirelesjr)
* LinkedIn: [seu link aqui]
