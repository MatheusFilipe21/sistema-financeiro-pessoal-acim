# ===================================================================
# ETAPA 1: Build (Construção do Spring Boot JAR)
# ===================================================================
# Usa a imagem oficial do Maven com Java 25 para compilar o código
FROM maven:3.9.14-eclipse-temurin-25-alpine AS build

# Define o diretório de trabalho no container
WORKDIR /app

# Copia o arquivo pom.xml para que as dependências sejam baixadas primeiro
COPY pom.xml .

# Baixa todas as dependências do Maven (otimização de cache)
RUN mvn dependency:go-offline -B

# Copia o código-fonte da aplicação
COPY src ./src

# Compila e empacota a aplicação, pulando os testes (DskipTests)
RUN mvn clean package -DskipTests

# ===================================================================
# ETAPA 2: Runtime (Execução da Aplicação)
# ===================================================================
# Usa uma imagem JRE (Java Runtime Environment) menor e mais segura
FROM eclipse-temurin:25-jre-alpine

# Argumento que armazena o caminho do artefato .jar (definido no pom.xml)
ARG JAR_FILE=target/backend-0.0.1-SNAPSHOT.jar

# Cria um usuário não-root ('spring') para a execução segura
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Define o diretório de trabalho
WORKDIR /app

# Copia o artefato .jar da etapa 'build' para a imagem final
COPY --from=build /app/${JAR_FILE} /app/app.jar

# Expõe a porta 8080 (padrão do Spring Boot)
EXPOSE 8080

# Comando para iniciar a aplicação Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
