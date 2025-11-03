package hospital.model;

import hospital.agents.ChildAgent;
import hospital.agents.AdultAgent;
import hospital.agents.ElderAgent;
import hospital.agents.HospitalDeCampanhaAgent;
import hospital.agents.PersonAgent;
import hospital.behaviors.AbstractFSMBehavior;
import hospital.enums.Local;
import hospital.logging.LoggerSMA;
import jade.core.AID;

import java.util.ArrayList;
import java.util.List;

public class Bairro {
    private final Local[][] mapa;
    private final int[] posicaoHospital;

    private AID hospitalAID;

    private HospitalDeCampanhaAgent hospitalAgente;

    private final List<ChildAgent> todosChild = new ArrayList<>();
    private final List<AdultAgent> todosAdult = new ArrayList<>();
    private final List<ElderAgent> todosElder = new ArrayList<>();

    // lista sincronizada com PersonAgent
    private final List<PersonAgent> mortosPendentes = new ArrayList<>();

    public Bairro() {
        mapa = new Local[4][4];

        mapa[0][0] = Local.CASA;
        mapa[0][1] = Local.ESCOLA;
        mapa[0][2] = Local.PARQUE;
        mapa[0][3] = Local.HOSPITAL;

        mapa[1][0] = Local.PARQUE;
        mapa[1][1] = Local.ATIVIDADE;
        mapa[1][2] = Local.ESCOLA;
        mapa[1][3] = Local.CASA;

        mapa[2][0] = Local.CASA;
        mapa[2][1] = Local.ATIVIDADE;
        mapa[2][2] = Local.PARQUE;
        mapa[2][3] = Local.ESCOLA;

        mapa[3][0] = Local.CASA;
        mapa[3][1] = Local.ATIVIDADE;
        mapa[3][2] = Local.PARQUE;
        mapa[3][3] = Local.CASA;

        posicaoHospital = new int[]{0, 3};
    }

    // ====================== NOVOS MÉTODOS PARA INTERFACE ======================
    /** Retorna o mapa completo (para uso na interface gráfica). */
    public Local[][] getMapa() {
        return mapa;
    }

    /** Retorna todos os agentes (crianças, adultos e idosos) na posição informada. */
    public List<PersonAgent> getAgentesNaPosicao(int x, int y) {
        List<PersonAgent> lista = new ArrayList<>();

        for (ChildAgent c : todosChild)
            if (c.getPosX() == x && c.getPosY() == y)
                lista.add(c);

        for (AdultAgent a : todosAdult)
            if (a.getPosX() == x && a.getPosY() == y)
                lista.add(a);

        for (ElderAgent e : todosElder)
            if (e.getPosX() == x && e.getPosY() == y)
                lista.add(e);

        return lista;
    }

    // ====================== HOSPITAL ======================
    public HospitalDeCampanhaAgent getHospitalAgente() { return hospitalAgente; }
    public void setHospitalAgente(HospitalDeCampanhaAgent hospitalAgente) { this.hospitalAgente = hospitalAgente; }

    public void setHospitalAID(AID hospitalAID){
        this.hospitalAID = hospitalAID;
    }

    public AID getHospitalAID(){
        return hospitalAID;
    }

    // ====================== MAPA ======================
    public Local getLocal(int x, int y) {
        if (x >= 0 && x < mapa.length && y >= 0 && y < mapa[0].length) return mapa[x][y];
        return null;
    }
    public int getLinhas() { return mapa.length; }
    public int getColunas() { return mapa[0].length; }
    public int[] getHospitalPos() { return posicaoHospital; }
    public boolean isHospitalLotado(List<AID> internados, int capacidade) { return internados.size() >= capacidade; }

    // ====================== AGENTES ======================
    public void adicionarAgenteChild(ChildAgent agente) { todosChild.add(agente); }
    public void adicionarAgenteAdult(AdultAgent agente) { todosAdult.add(agente); }
    public void adicionarAgenteElder(ElderAgent agente) { todosElder.add(agente); }

    public List<ChildAgent> getTodosChild() { return todosChild; }
    public List<AdultAgent> getTodosAdult() { return todosAdult; }
    public List<ElderAgent> getTodosElder() { return todosElder; }

    // ====================== REMOÇÃO SINCRONIZADA ======================

    /** Marca o agente como morto (chamado por PersonAgent ao morrer). */
    public synchronized void marcarComoMorto(PersonAgent agente) {
        if (!mortosPendentes.contains(agente)) {
            mortosPendentes.add(agente);
            LoggerSMA.warn(agente, "☠️ Marcando %s para remoção.", agente.getLocalName());
        }
    }

    /** Remove mortos marcados das listas principais. */
    public synchronized void removerAgentesMortos() {
        if (!mortosPendentes.isEmpty()) {
            for (PersonAgent morto : new ArrayList<>(mortosPendentes)) {
                if (morto instanceof ChildAgent c) todosChild.remove(c);
                else if (morto instanceof AdultAgent a) todosAdult.remove(a);
                else if (morto instanceof ElderAgent e) todosElder.remove(e);

                mortosPendentes.remove(morto);
                LoggerSMA.event(morto, "🧹 Removendo %s do bairro (morto).", morto.getLocalName());
            }
        }

        // (Fallback) caso algum FSM ainda sinalize isMorto()
        todosChild.removeIf(c -> {
            boolean morto = isMortoFSM(c);
            if (morto) {
                LoggerSMA.event(c, "🧹 Removendo %s do bairro (FSM morto).", c.getLocalName());
                return true;
            }
            return false;
        });

        todosAdult.removeIf(a -> {
            boolean morto = isMortoFSM(a);
            if (morto) {
                LoggerSMA.event(a, "🧹 Removendo %s do bairro (FSM morto).", a.getLocalName());
                return true;
            }
            return false;
        });

        todosElder.removeIf(e -> {
            boolean morto = isMortoFSM(e);
            if (morto) {
                LoggerSMA.event(e, "🧹 Removendo %s do bairro (FSM morto).", e.getLocalName());
                return true;
            }
            return false;
        });
    }

    private static boolean isMortoFSM(PersonAgent p) {
        Object beh = p.getBehavior();
        if (beh instanceof AbstractFSMBehavior) {
            @SuppressWarnings("unchecked")
            AbstractFSMBehavior<PersonAgent> b = (AbstractFSMBehavior<PersonAgent>) beh;
            return b.isMorto();
        }
        return false;
    }

    // ====================== LOCALIZAÇÃO ======================
    public List<Object> getTodosAgentesNoLocal(int x, int y) {
        List<Object> lista = new ArrayList<>();
        lista.addAll(getAgentesNoLocalChild(x, y));
        lista.addAll(getAgentesNoLocalAdult(x, y));
        lista.addAll(getAgentesNoLocalElder(x, y));
        return lista;
    }

    public List<ChildAgent> getAgentesNoLocalChild(int x, int y) {
        List<ChildAgent> lista = new ArrayList<>();
        for (ChildAgent agente : todosChild)
            if (agente.getPosX() == x && agente.getPosY() == y)
                lista.add(agente);
        return lista;
    }

    public List<AdultAgent> getAgentesNoLocalAdult(int x, int y) {
        List<AdultAgent> lista = new ArrayList<>();
        for (AdultAgent agente : todosAdult)
            if (agente.getPosX() == x && agente.getPosY() == y)
                lista.add(agente);
        return lista;
    }

    public List<ElderAgent> getAgentesNoLocalElder(int x, int y) {
        List<ElderAgent> lista = new ArrayList<>();
        for (ElderAgent agente : todosElder)
            if (agente.getPosX() == x && agente.getPosY() == y)
                lista.add(agente);
        return lista;
    }

    // ====================== VISUALIZAÇÃO ======================
    public void imprimirEstado(int tick) {
        removerAgentesMortos();

        LoggerSMA.setTick(tick);
        LoggerSMA.system("\n📅 Tick %d:", tick);

        for (int i = 0; i < getLinhas(); i++) {
            StringBuilder linha = new StringBuilder();
            for (int j = 0; j < getColunas(); j++) {
                List<Object> agentesAqui = getTodosAgentesNoLocal(i, j);
                StringBuilder celula = new StringBuilder("[ ");

                if (mapa[i][j] == Local.HOSPITAL) celula.append("🏥 ");

                for (Object a : agentesAqui) {
                    String nome;
                    boolean infectado = false;

                    if (a instanceof ChildAgent c) {
                        nome = c.getLocalName();
                        infectado = c.isInfectado();
                    } else if (a instanceof AdultAgent a2) {
                        nome = a2.getLocalName();
                        infectado = a2.isInfectado();
                    } else if (a instanceof ElderAgent e) {
                        nome = e.getLocalName();
                        infectado = e.isInfectado();
                    } else {
                        nome = "?";
                    }

                    if (infectado) celula.append("🤮").append(nome).append(", ");
                    else celula.append(nome).append(", ");
                }

                if (!agentesAqui.isEmpty()) celula.setLength(celula.length() - 2);
                celula.append(" ] ");
                linha.append(celula);
            }
            LoggerSMA.system(linha.toString());
        }

        long totalInfectados = todosChild.stream().filter(ChildAgent::isInfectado).count()
                + todosAdult.stream().filter(AdultAgent::isInfectado).count()
                + todosElder.stream().filter(ElderAgent::isInfectado).count();

        long totalAgentes = todosChild.size() + todosAdult.size() + todosElder.size();

        LoggerSMA.info(null,
                "\n📊 Estatísticas: Infectados = %d | Total de agentes = %d | Hospital em (%d,%d)",
                totalInfectados, totalAgentes, posicaoHospital[0], posicaoHospital[1]);

        LoggerSMA.flushTick();
    }
}
