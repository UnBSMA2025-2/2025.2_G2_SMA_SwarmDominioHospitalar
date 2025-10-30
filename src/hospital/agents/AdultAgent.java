package hospital.agents;

import hospital.logging.LoggerSMA;
import hospital.model.Bairro;
import hospital.model.Doenca;
import hospital.behaviors.AdultFSMBehavior;
import jade.core.behaviours.TickerBehaviour;

import java.util.Random;

public class AdultAgent extends PersonAgent {

    protected Random rand = new Random();

    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        if (args != null && args.length > 1 && args[1] instanceof String[]) {
            String[] dados = (String[]) args[1];
            StringBuilder sb = new StringBuilder();
            for (String d : dados) sb.append(d).append(" | ");
            LoggerSMA.info(this, "🧑‍💼 %s criado(a). Descrição: %s", getLocalName(), sb.toString());
        }

        var fsm = new AdultFSMBehavior(this, 1000, bairro);
        setBehavior(fsm);
        addBehaviour(fsm);

        LoggerSMA.event(this, "🧩 %s configurado com FSM AdultFSMBehavior e adicionado ao bairro.", getLocalName());
    }

    @Override
    protected TickerBehaviour criarBehaviour() {
        return new AdultFSMBehavior(this, 1000, bairro);
    }

    @Override
    protected void adicionarAoBairro() {
        bairro.adicionarAgenteAdult(this);
        LoggerSMA.info(this, "🏘️ %s registrado no bairro como adulto.", getLocalName());
    }

    @Override
    protected Doenca criarDoenca() {
        return new Doenca("COVID", 0.1, 2.0);
    }

    @Override
    protected String getEmoji() {
        return "🧑‍💼";
    }

    @Override
    protected void configurarVulnerabilidade() {
        double v = rand.nextGaussian() * 0.15 + 0.5; // média 0.5, desvio 0.15
        this.vulnerabilidade = Math.max(0.0, Math.min(1.0, v)); // limita entre 0–1
    }
}
