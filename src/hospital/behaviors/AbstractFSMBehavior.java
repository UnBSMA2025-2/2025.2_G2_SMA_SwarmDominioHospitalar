package hospital.behaviors;

import hospital.agents.PersonAgent;
import hospital.enums.Local;
import hospital.model.Bairro;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractFSMBehavior<T extends PersonAgent> extends TickerBehaviour {

    protected final Bairro bairro;
    protected final Random rand = new Random();
    protected int diasCompletos = 0;
    protected final int LIMITE_DIAS = 5;
    protected int tickDoDia = 0;
    private static List<PersonAgent> aInfectarNoTick = new ArrayList<>(); // controla o tick global
    private int ticksDesdeInfeccao = 0;
    private int ticksNoHospital = 0;
    private boolean tentouHospital = false;

    public AbstractFSMBehavior(T agente, long period, Bairro bairro) {
        super(agente, period);
        this.bairro = bairro;
    }

    @Override
    protected void onTick() {
        T agente = (T) myAgent;

        // ===================== ATRIBUI CASA FIXA =====================
        if (!agente.isCasaDefinida()) {
            int[] posCasa = encontrarCasaDisponivel(agente);
            agente.setCasa(posCasa[0], posCasa[1]);
            agente.setPos(posCasa[0], posCasa[1]);
            agente.setCasaDefinida(true);
        }

        // ===================== ATUALIZAR CONTAGEM DE INFECÇÃO =====================
        if (agente.isInfectado() && !agente.isHospitalizado()) {
            ticksDesdeInfeccao++;
        } else if (!agente.isInfectado()) {
            // Reset quando cura
            ticksDesdeInfeccao = 0;
        }

        // ===================== LÓGICA DE HOSPITAL =====================
        // SÓ permite hospitalização após pelo menos 2 ticks de infecção (garante 1 tick completo)
        if (agente.isInfectado() && !agente.isHospitalizado() && !tentouHospital && ticksDesdeInfeccao >= 3) {
            tentarHospitalizacao(agente);
        }

        // ===================== PROCESSAR CURA NO HOSPITAL =====================
        if (agente.isHospitalizado()) {
            processarCura(agente);
            ticksNoHospital++;
        }

        // ===================== ROTINA DIÁRIA =====================
        Local localAtual;
        if (agente.isHospitalizado()) {
            localAtual = Local.HOSPITAL;
            // Mantém no hospital único
            int[] posHospital = bairro.getHospital();
            agente.setPos(posHospital[0], posHospital[1]);
        } else {
            localAtual = definirLocalDoDia(agente, tickDoDia);
            
            if (localAtual == Local.CASA) {
                agente.setPos(agente.getHomeX(), agente.getHomeY());
            } else {
                int[] pos = encontrarPosicaoLocal(localAtual, agente);
                agente.setPos(pos[0], pos[1]);
            }
        }

        // ===================== CHECA INFECÇÃO =====================
        checarInfeccaoGenerica(agente, bairro.getTodosAgentesNoLocal(agente.getPosX(), agente.getPosY()));

        // ===================== PRÓXIMO TICK =====================
        tickDoDia++;
        if (tickDoDia > 2) {
            tickDoDia = 0;
            diasCompletos++;
            tentouHospital = false; // Permite tentar hospital novamente no próximo dia
            if (diasCompletos >= LIMITE_DIAS) {
                myAgent.doDelete();
                return;
            }
        }

        // ===================== SINCRONIZAÇÃO GLOBAL DE TICK =====================
        // 1. Notifica o controlador que este agente terminou o tick atual
        agente.notifyTickDone(tickDoDia);

        // 2. Espera até receber liberação do SyncControllerAgent para continuar
        agente.waitForNextTick();
    }

    // ===================== MÉTODOS DE HOSPITAL =====================
    protected void tentarHospitalizacao(T agente) {
        // Verifica se o hospital não está lotado
        if (bairro.isHospitalLotado()) {
            tentouHospital = true;
        } else {
            // Hospital com vagas
            double chanceHospital = calcularChanceHospital(agente);
            if (rand.nextDouble() < chanceHospital) {
                realizarHospitalizacao(agente);
            } else {
                tentouHospital = true;
            }
        }
    }

    protected void realizarHospitalizacao(T agente) {
        int[] hospital = bairro.getHospital();
        agente.setHospitalizado(true);
        agente.setPos(hospital[0], hospital[1]);
        int vagasRestantes = 5 - bairro.getTodosAgentesNoLocal(hospital[0], hospital[1]).size();
        System.out.println("🏥 " + agente.getLocalName() + " foi hospitalizado após " + ticksDesdeInfeccao + " ticks infectado! (Vagas: " + vagasRestantes + ")");
        tentouHospital = true;
    }

    protected double calcularChanceHospital(T agente) {
        double baseChance = 0.2; // 30% base
        
        // Idosos têm prioridade máxima
        if (agente instanceof hospital.agents.ElderAgent) {
            baseChance += 0.3;
        }
        
        // Aumenta chance com tempo de infecção (agora baseado em ticks)
        // Cada dia completo (3 ticks) aumenta 20%
        baseChance += (ticksDesdeInfeccao / 3) * 0.2;

        return Math.min(baseChance, 0.8);
    }

    protected void processarCura(T agente) {
        double chanceCura = calcularChanceCura(agente);
        
        if (rand.nextDouble() < chanceCura) {
            agente.setInfectado(false);
            agente.setHospitalizado(false);
            ticksDesdeInfeccao = 0;
            System.out.println("✅ " + agente.getLocalName() + " foi curado após " + (ticksNoHospital) + " ticks hospitalizado! 🎉");
        }
    }

    protected double calcularChanceCura(T agente) {
        double baseCura = 0.0; // 0% base
        baseCura += ticksNoHospital * 0.1; // Aumenta 15% a cada tick no hospital
        
        // Idosos têm menor chance de cura
        if (agente instanceof hospital.agents.ElderAgent) {
            baseCura -= 0.1;
        }
        
        return Math.max(baseCura, 0.0);
    }

    // ===================== MÉTODOS ABSTRATOS =====================
    protected abstract Local definirLocalDoDia(T agente, int tickDoDia);
    protected abstract int[] encontrarCasaDisponivel(T agente);

    // ===================== MÉTODO GENÉRICO PARA POSIÇÃO =====================
    protected int[] encontrarPosicaoLocal(Local local, T agente) {
        List<int[]> posicoes = new ArrayList<>();

        for (int i = 0; i < bairro.getLinhas(); i++) {
            for (int j = 0; j < bairro.getColunas(); j++) {
                if (bairro.getLocal(i, j) == local) {
                    if (!(i == agente.getPosX() && j == agente.getPosY())) {
                        posicoes.add(new int[]{i, j});
                    }
                }
            }
        }

        if (posicoes.isEmpty()) return new int[]{agente.getPosX(), agente.getPosY()};
        return posicoes.get(rand.nextInt(posicoes.size()));
    }

    // ===================== MÉTODO GENÉRICO DE INFECÇÃO =====================
    protected void checarInfeccaoGenerica(T agente, List<Object> agentesNoLocal) {
        List<PersonAgent> paraInfectar = new ArrayList<>();

        for (Object outro : agentesNoLocal) {
            if (outro instanceof PersonAgent p) {
                if (p.isInfectado() && p.getDoenca() != null && !agente.isInfectado()) {
                    double pTrans = p.getDoenca().getBeta() * p.getDoenca().getInfectividade();
                    if (rand.nextDouble() < pTrans) {
                        paraInfectar.add(agente); // marca para infectar
                    }
                }
            }
        }

        // Atualiza a lista global de forma sincronizada
        if (!paraInfectar.isEmpty()) {
            synchronized (aInfectarNoTick) {
                aInfectarNoTick.addAll(paraInfectar);
            }
        }
    }

    public static List<PersonAgent> getAInfectarNoTick() {
        return aInfectarNoTick;
    }
}