# Product-Service

Microsserviço responsável pelo cadastro e consulta de produtos da loja. Expõe uma API REST, persiste dados em PostgreSQL e usa Redis para cache de leitura.

## Responsabilidades

- Criar, listar, buscar e remover produtos
- Garantir que operações de escrita sejam restritas a usuários com papel `ADMIN`
- Cachear respostas de leitura por produto no Redis (TTL de 5 minutos)

## Arquitetura interna

```
ProductResource     ← controller REST (implementa ProductController do módulo product)
    └── ProductService   ← regras de negócio + anotações de cache
            └── ProductRepository  ← Spring Data (CrudRepository)
                    └── ProductModel  ← entidade JPA (tabela products.products)
                            └── Product  ← objeto de domínio (classe interna, não JPA)

ProductParser       ← conversões entre Product (domínio), ProductIn e ProductOut
RedisConfig         ← configuração do serializador JSON para o cache
```

> `Product` é a representação interna usada entre as camadas do serviço; `ProductModel` cuida da persistência e `ProductIn`/`ProductOut` são os DTOs da API (vindos do módulo `product`).

## Endpoints

Todos os endpoints ficam expostos na porta `8080` e são roteados pelo gateway em `/products/**`.

### `GET /products`
Lista todos os produtos cadastrados.

**Resposta `200 OK`:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "name": "Arroz",
    "price": 5.99,
    "unit": "kg"
  }
]
```

---

### `GET /products/{id}`
Busca um produto pelo UUID. O resultado é cacheado no Redis com chave `products::{id}`.

**Parâmetros de path:**

| Parâmetro | Tipo     | Descrição      |
|-----------|----------|----------------|
| `id`      | `String` | UUID do produto|

**Respostas:**
- `200 OK` — produto encontrado
- `404 Not Found` — produto não existe

---

### `POST /products`
Cria um novo produto. Requer papel `ADMIN`.

**Header obrigatório:**
```
role: ADMIN
```

**Corpo da requisição:**
```json
{
  "name": "Arroz",
  "price": 5.99,
  "unit": "kg"
}
```

**Respostas:**
- `201 Created` — produto criado; header `Location` aponta para `/products/{id}`
- `403 Forbidden` — header `role` ausente ou diferente de `ADMIN`

---

### `DELETE /products/{id}`
Remove um produto pelo UUID e evicta sua entrada do cache. Requer papel `ADMIN`.

**Header obrigatório:**
```
role: ADMIN
```

**Respostas:**
- `204 No Content` — produto removido
- `403 Forbidden` — sem permissão

---

### `GET /products/search`
Busca produtos pelo nome (parcial, case-insensitive). Se `name` for omitido ou vazio, retorna todos os produtos.

**Parâmetros de query:**

| Parâmetro | Tipo     | Obrigatório | Descrição                        |
|-----------|----------|-------------|----------------------------------|
| `name`    | `String` | Não         | Trecho do nome a ser pesquisado  |

**Resposta `200 OK`:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "name": "Arroz",
    "price": 5.99,
    "unit": "kg"
  }
]
```

---

### `GET /products/health-check`
Verifica se o serviço está no ar.

**Resposta `200 OK`** (sem corpo)

## Cache (Redis)

| Método de serviço | Anotação         | Comportamento                                    |
|-------------------|------------------|--------------------------------------------------|
| `create`          | `@CachePut`      | Insere o produto recém-criado no cache           |
| `findById`        | `@Cacheable`     | Retorna do cache se existir; senão consulta o DB |
| `delete`          | `@CacheEvict`    | Remove a entrada do cache ao deletar             |
| `findByAll`       | —                | Sempre vai ao banco (lista completa)             |
| `findByNameLike`  | —                | Sempre vai ao banco (busca por nome)             |

- TTL: **5 minutos**
- Valores nulos não são cacheados (`cache-null-values: false`)
- Serialização: JSON via `GenericJackson2JsonRedisSerializer`

## Banco de dados

- **SGBD:** PostgreSQL
- **Schema:** `products`
- **Migrations:** Flyway (executadas automaticamente na inicialização)

### Tabela `products.products`

| Coluna  | Tipo           | Restrições              |
|---------|----------------|-------------------------|
| `id`    | `UUID`         | PK, gerado automaticamente |
| `name`  | `VARCHAR(256)` | NOT NULL                |
| `price` | `REAL`         | NOT NULL                |
| `unit`  | `VARCHAR(50)`  | —                       |

### Migrations

| Arquivo                                | Descrição                   |
|----------------------------------------|-----------------------------|
| `V2026.05.26.001__create_schema.sql`   | Cria o schema `products`    |
| `V2026.05.26.002__create_table.sql`    | Cria a tabela `products`    |

## Variáveis de ambiente

| Variável            | Descrição                          | Padrão      |
|---------------------|------------------------------------|-------------|
| `DATABASE_HOST`     | Host do PostgreSQL                 | —           |
| `DATABASE_PORT`     | Porta do PostgreSQL                | —           |
| `DATABASE_DB`       | Nome do banco de dados             | —           |
| `DATABASE_USERNAME` | Usuário do banco                   | —           |
| `DATABASE_PASSWORD` | Senha do banco                     | —           |
| `REDIS_HOST`        | Host do Redis                      | `localhost` |
| `REDIS_PORT`        | Porta do Redis                     | `6379`      |

## Tecnologias

| Tecnologia              | Versão   |
|-------------------------|----------|
| Java                    | 25       |
| Spring Boot             | 4.0.6    |
| Spring Cloud            | 2025.1.1 |
| Spring Data JPA         | —        |
| Spring Data Redis       | —        |
| Spring Cache            | —        |
| PostgreSQL              | —        |
| Redis                   | —        |
| Flyway                  | —        |
| Lombok                  | —        |

## Como rodar localmente

### Pré-requisitos

- Java 25
- PostgreSQL acessível
- Redis acessível
- Módulo `api/product` instalado no Maven local (`mvn install`)

### Executar

```bash
cd api/product-service

export DATABASE_HOST=localhost
export DATABASE_PORT=5432
export DATABASE_DB=store
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres

mvn spring-boot:run
```

O serviço sobe na porta `8080` e aplica as migrations automaticamente.

### Via Docker

O serviço é iniciado pelo `compose.yaml` na raiz do projeto, junto com PostgreSQL e Redis. O hostname do container é `product`.
