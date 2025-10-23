package hospital.behaviors;

import hospital.enums.Local;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

public class AdultFSMBehavior extends FSMBehaviour {

    private int diasCompletos = 0;
    private final int LIMITE_DIAS = 3;

    public AdultFSMBehavior(Agent a) {
        super(a);

        registerFirstState(new Casa(), String.valueOf(Local.CASA));
        registerState(new Trabalho(), String.valueOf(Local.TRABALHO));
        registerState(new Festa(), String.valueOf(Local.FESTA));
        registerState(new Parque(), String.valueOf(Local.PARQUE));
        registerLastState(new FimDoDia(), String.valueOf(Local.FIM));

        registerTransition(String.valueOf(Local.CASA), String.valueOf(Local.TRABALHO), 1);
        registerTransition(String.valueOf(Local.TRABALHO), String.valueOf(Local.FESTA), 2);
        registerTransition(String.valueOf(Local.TRABALHO), String.valueOf(Local.PARQUE), 3);
        registerTransition(String.valueOf(Local.TRABALHO), String.valueOf(Local.CASA), 4);
        registerTransition(String.valueOf(Local.FESTA), String.valueOf(Local.CASA), 5);
        registerTransition(String.valueOf(Local.PARQUE), String.valueOf(Local.CASA), 6);
        registerTransition(String.valueOf(Local.CASA), String.valueOf(Local.FIM), 9);
    }

    // Função auxiliar para pausas entre ações
    private void esperar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class Casa extends OneShotBehaviour {
        private int transition;

        @Override
        public void action() {
            diasCompletos++;
            System.out.println("🏡 [" + myAgent.getLocalName() + "] está em casa. Acordando e se preparando para o trabalho... (dia " + diasCompletos + ")");
            esperar(1000);

            if (diasCompletos >= LIMITE_DIAS) {
                System.out.println("🛏️ [" + myAgent.getLocalName() + "] finalizou o dia " + diasCompletos + " e vai dormir definitivamente.");
                esperar(1500);
                transition = 9; // fim
            } else {
                transition = 1; // vai para o trabalho
            }
        }

        @Override
        public int onEnd() {
            return transition;
        }
    }

    private class Trabalho extends OneShotBehaviour {
        private int transition;

        @Override
        public void action() {
            System.out.println("💼 [" + myAgent.getLocalName() + "] está trabalhando... respondendo e-mails e participando de reuniões.");
            esperar(1200);

            // Escolhe aleatoriamente o próximo destino após o trabalho
            int escolha = (int) (Math.random() * 3);
            switch (escolha) {
                case 0 -> {
                    System.out.println("🍷 [" + myAgent.getLocalName() + "] decidiu ir a uma festa após o expediente.");
                    transition = 2;
                }
                case 1 -> {
                    System.out.println("🌳 [" + myAgent.getLocalName() + "] decidiu relaxar no parque após o trabalho.");
                    transition = 3;
                }
                default -> {
                    System.out.println("🚗 [" + myAgent.getLocalName() + "] terminou o expediente e está voltando para casa.");
                    transition = 4;
                }
            }
        }

        @Override
        public int onEnd() {
            return transition;
        }
    }

    private class Festa extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🎉 [" + myAgent.getLocalName() + "] está se divertindo em uma festa!");
            esperar(1000);
        }

        @Override
        public int onEnd() {
            return 5; // volta pra casa
        }
    }

    private class Parque extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🌳 [" + myAgent.getLocalName() + "] está caminhando no parque para relaxar.");
            esperar(1000);
        }

        @Override
        public int onEnd() {
            return 6; // volta pra casa
        }
    }

    private class FimDoDia extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🌙 [" + myAgent.getLocalName() + "] encerrou suas atividades e foi dormir. Encerrando agente...");
            esperar(1000);
            myAgent.doDelete();
        }
    }
}
