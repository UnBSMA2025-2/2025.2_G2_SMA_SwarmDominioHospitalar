#!/bin/bash

echo "=== Compilando projeto Hospital Simulation ==="

# Criar diretório bin se não existir
mkdir -p bin

# Passo 1: Encontrar todos os arquivos .java
echo "1. Buscando arquivos .java..."
find src -name "*.java" > sources.txt

# Verificar se encontrou arquivos
if [ ! -s sources.txt ]; then
    echo "❌ ERRO: Nenhum arquivo .java encontrado em src/"
    exit 1
fi

echo "✅ Encontrados $(wc -l < sources.txt) arquivos .java"

# Passo 2: Compilar
echo "2. Compilando arquivos..."
javac -cp "jarsExternos/*" -d bin @sources.txt

if [ $? -ne 0 ]; then
    echo "❌ ERRO: Falha na compilação"
    exit 1
fi

echo "✅ Compilação concluída com sucesso!"

# Passo 3: Executar
echo "3. Iniciando simulação..."
echo "=== LOGS DA SIMULAÇÃO ==="
java -cp "bin:jarsExternos/jade-4.6.1-6874.jar:jarsExternos/jcommon-1.0.24.jar:jarsExternos/jfreechart-1.5.4.jar" hospital.MainContainer

echo "=== SIMULAÇÃO FINALIZADA ==="