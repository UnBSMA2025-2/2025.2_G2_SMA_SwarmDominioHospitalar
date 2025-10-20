package hospital.agents;

import jade.core.Agent;
import hospital.behaviors.ChildFSMBehavior;

public class ChildAgent extends Agent {

    @Override
    protected void setup() {
        Object[] args = getArguments();

        String descricao = "(sem descrição)";
        if (args != null && args.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                sb.append(arg.toString()).append(" | ");
            }
            descricao = sb.toString();
        }

        System.out.println("👶 " + getLocalName() + " foi criado! Descrição: " + descricao);
        addBehaviour(new ChildFSMBehavior(this));
    }

    @Override
    protected void takeDown() {
        System.out.println("👋 " + getLocalName() + " terminou seu dia e está sendo desligado.");
    }
}