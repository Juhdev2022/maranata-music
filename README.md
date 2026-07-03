cat > README.md << 'EOF'
# Maranata Music

App de escalas musicais para o ministério Maranata Music.

**Stack:** Java 17 · Spring Boot 3 · PostgreSQL · React (PWA)
**Status:** 🚧 Em desenvolvimento — Fase 1 (fundação)

## Rodar localmente

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

API disponível em `http://localhost:8080`.
EOF

git add README.md
git commit -m "docs: README inicial"
