package hospital.bdi;

import jade.core.AID;
import java.util.*;

public class HospitalBeliefs {
    private int capacidadeMax;
    private int recursos; // quantidade de recursos disponíveis
    private int recursosMax;
    private final List<AID> internados = new ArrayList<>();
    private final Map<AID, Integer> diasInternado = new HashMap<>();

    public HospitalBeliefs(int capacidadeMax, int recursosMax) {
        this.capacidadeMax = capacidadeMax;
        this.recursosMax = recursosMax;
        this.recursos = recursosMax;
    }

    public boolean temVaga() {
        return internados.size() < capacidadeMax;
    }

    public void internar(AID paciente) {
        internados.add(paciente);
        diasInternado.put(paciente, 0);
    }

    public void liberar(AID paciente) {
        internados.remove(paciente);
        diasInternado.remove(paciente);
    }

    public void consumirRecursos(int qtd) {
        recursos = Math.max(0, recursos - qtd);
    }

    public void recarregarRecursos() {
        recursos = recursosMax;
    }

    public boolean temRecursosSuficientes(int custo) {
        return recursos >= custo;
    }

    public List<AID> getInternados() { return internados; }
    public Map<AID, Integer> getDiasInternado() { return diasInternado; }
    public int getRecursos() { return recursos; }
    public int getCapacidade() { return capacidadeMax; }
}