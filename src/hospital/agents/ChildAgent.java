package hospital.agents;

import hospital.model.Bairro;
import hospital.model.Doenca;
import hospital.behaviors.ChildFSMBehavior;
import jade.core.behaviours.TickerBehaviour;

import java.util.Random;

public class ChildAgent extends PersonAgent {

    protected Random rand = new Random();
    private boolean pacienteZero = false;

    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        if (args != null && args.length > 2) {
            String[] dados = (String[]) args[1];
            boolean pacienteZero = (boolean) args[2];
            if (pacienteZero) setPacienteZero(true);

            StringBuilder sb = new StringBuilder();
            for (String d : dados) sb.append(d).append(" | ");
            System.out.println("👶 " + getLocalName() + " descrição: " + sb.toString());
        }

        // Garante que o FSM seja salvo no PersonAgent
        var fsm = new ChildFSMBehavior(this, 1000, bairro);
        setBehavior(fsm);
        addBehaviour(fsm);
    }


    @Override
    protected TickerBehaviour criarBehaviour() {
        return new ChildFSMBehavior(this, 1000, bairro);
    }

    @Override
    protected void adicionarAoBairro() {
        bairro.adicionarAgenteChild(this);
    }

    @Override
    protected Doenca criarDoenca() {
        return new Doenca("COVID", 0.1, 2.0);
    }

    @Override
    protected String getEmoji() {
        return "👶";
    }

    @Override
    protected void configurarVulnerabilidade() {
        double v = Math.pow(rand.nextDouble(), 2.5); // maioria baixa, raros altos
        this.vulnerabilidade = 0.1 + v * 0.9; // faixa total 0.1–1.0
    }

    public void setPacienteZero(boolean pacienteZero) {
        this.pacienteZero = pacienteZero;
        if (pacienteZero) this.infectado = true;
    }
}