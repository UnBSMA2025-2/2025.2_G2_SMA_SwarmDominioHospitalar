package hospital.agents;

import hospital.behaviors.HospitalDeCampanhaBehaviour;
import hospital.model.Bairro;
import jade.core.AID;
import jade.core.Agent;
import java.util.*;

public class HospitalDeCampanhaAgent extends Agent {

    private int capacidade = 5;
    private final List<AID> internados = new ArrayList<>();
    private final Map<AID, Integer> diasInternado = new HashMap<>();

    private int posX;
    private int posY;
    private Bairro bairro;

    @Override
    protected void setup() {
        // Recupera o bairro passado como argumento
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.bairro = (Bairro) args[0];
            this.posX = bairro.getHospitalPos()[0];
            this.posY = bairro.getHospitalPos()[1];

            // Registra o hospital no bairro (para referência global)
            bairro.setHospitalAgente(this);
        }

        System.out.println("🏥 " + getLocalName() + " pronto! Capacidade: " + capacidade +
                " | Posição: (" + posX + "," + posY + ")");

        // Inicia o comportamento de gestão hospitalar
        addBehaviour(new HospitalDeCampanhaBehaviour(this, 1000));
    }

    // ===================== MÉTODOS DE GESTÃO =====================

    public boolean temVaga() {
        return internados.size() < capacidade;
    }

    public void admitirPaciente(AID paciente) {
        internados.add(paciente);
        diasInternado.put(paciente, 0);
        System.out.println("🏥 " + paciente.getLocalName() + " foi internado no " + getLocalName());
    }

    public void liberarPaciente(AID paciente) {
        internados.remove(paciente);
        diasInternado.remove(paciente);
        System.out.println("💚 " + paciente.getLocalName() + " recebeu alta no " + getLocalName());
    }

    // ===================== GETTERS =====================

    public List<AID> getInternados() {
        return internados;
    }

    public Map<AID, Integer> getDiasInternado() {
        return diasInternado;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public Bairro getBairro() {
        return bairro;
    }

    public int getCapacidade() {
        return capacidade;
    }
}
