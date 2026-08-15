FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY src ./src
RUN mkdir -p out && javac --add-modules jdk.httpserver -d out $(find src -name "*.java")

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/out ./out
COPY public ./public
RUN mkdir -p data
ENV PORT=10000
EXPOSE 10000
CMD ["sh", "-c", "java --add-modules jdk.httpserver -cp out com.university.research.Main"]
