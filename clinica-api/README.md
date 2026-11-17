# Clínica API

API REST (Spring Boot 3 + Spring Data JPA + Spring Security/JWT) para gerenciar **pacientes**, **profissionais** e **agendamentos**.

## Como executar

Requisitos: Java 17+ e Maven.

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. O banco é H2 em arquivo (`./data/clinicadb`), então os dados persistem entre execuções.
Na primeira execução são criados dois usuários e alguns dados de exemplo:

| Login | Senha     | Perfil |
|-------|-----------|--------|
| admin | admin123  | ADMIN  |
| comum | comum123  | COMUM  |

## Arquitetura

```
controller  -> recebe/valida a requisição, devolve DTOs (sem regra de negócio)
service     -> regras de negócio e orquestração
repository  -> Spring Data JPA (JpaRepository + Specification)
model       -> entidades JPA
dto         -> records de request/response
security    -> JWT (JwtService, JwtAuthFilter, UserDetailsService)
exception   -> exceções de domínio + @RestControllerAdvice
```

### Modelo e relacionamentos 1:N

- `Paciente` 1 ──── N `Agendamento`
- `Profissional` 1 ──── N `Agendamento`

## Autenticação e autorização

`POST /auth/login` retorna um token JWT. Envie-o nas demais requisições: `Authorization: Bearer <token>`.

| Operação                                              | ADMIN | COMUM |
|-------------------------------------------------------|:-----:|:-----:|
| Login                                                 |  ✔   |  ✔   |
| GET (qualquer consulta)                               |  ✔   |  ✔   |
| POST/PUT/PATCH em pacientes e agendamentos            |  ✔   |  ✔   |
| POST/PUT em profissionais                             |  ✔   |  ✖   |
| DELETE (qualquer recurso)                             |  ✔   |  ✖   |
| `/usuarios` (criar/listar usuários)                   |  ✔   |  ✖   |

Sem token → `401`. Token válido sem permissão → `403`.

## Endpoints

| Método | Rota                              | Descrição |
|--------|-----------------------------------|-----------|
| POST   | `/auth/login`                     | Login |
| POST/GET | `/usuarios`                     | Criar / listar usuários (ADMIN) |
| POST   | `/pacientes`                      | Criar |
| GET    | `/pacientes?nome=&cpf=`           | Listar com filtros opcionais |
| GET    | `/pacientes/{id}`                 | Buscar por ID |
| GET    | `/pacientes/{id}/agendamentos`    | **Filtro no path**: agendamentos do paciente |
| PUT    | `/pacientes/{id}`                 | Atualizar |
| DELETE | `/pacientes/{id}`                 | Excluir (ADMIN) |
| POST/GET/PUT/DELETE | `/profissionais`, `/profissionais/{id}` | CRUD; `GET /profissionais?nome=&especialidade=` |
| GET    | `/profissionais/{id}/agendamentos`| **Filtro no path**: agenda do profissional |
| POST   | `/agendamentos`                   | Criar |
| GET    | `/agendamentos?pacienteId=&profissionalId=&status=&de=&ate=` | **Múltiplos parâmetros** (todos opcionais; datas `yyyy-MM-dd`) |
| GET    | `/agendamentos/{id}`              | Buscar por ID |
| PUT    | `/agendamentos/{id}`              | Atualizar (só se AGENDADO) |
| PATCH  | `/agendamentos/{id}/status`       | Concluir ou cancelar |
| DELETE | `/agendamentos/{id}`              | Excluir (ADMIN) |

## Regras de negócio (camada de serviço)

- CPF de paciente e registro de profissional são únicos.
- Paciente/profissional com agendamentos não pode ser excluído.
- Agendamento só pode ser criado/alterado para data futura, entre 07:00 e 19:00.
- Não pode haver dois agendamentos (não cancelados) no mesmo horário para o mesmo profissional **nem** para o mesmo paciente.
- Só agendamentos `AGENDADO` podem ser editados; transições de status: `AGENDADO → CONCLUIDO | CANCELADO`.
- Só é possível concluir um atendimento cujo horário já passou.
- Agendamentos `CONCLUIDO` não podem ser excluídos (histórico).

## Exemplos (curl)

```bash
# 1) Login
TOKEN=$(curl -s -X POST localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"login":"admin","senha":"admin123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

# 2) Criar agendamento
curl -X POST localhost:8080/agendamentos -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"pacienteId":1,"profissionalId":1,"dataHora":"2026-12-10T09:30:00","observacoes":"Primeira consulta"}'

# 3) Filtro no path
curl localhost:8080/pacientes/1/agendamentos -H "Authorization: Bearer $TOKEN"

# 4) Múltiplos parâmetros
curl "localhost:8080/agendamentos?profissionalId=1&status=AGENDADO&de=2026-12-01&ate=2026-12-31" \
  -H "Authorization: Bearer $TOKEN"

# 5) Cancelar
curl -X PATCH localhost:8080/agendamentos/1/status -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"status":"CANCELADO"}'
```
