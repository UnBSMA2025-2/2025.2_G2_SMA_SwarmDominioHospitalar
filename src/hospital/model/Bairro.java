package hospital.model;

import hospital.agents.ChildAgent;
import hospital.agents.AdultAgent;
import hospital.agents.ElderAgent;
import hospital.agents.HospitalDeCampanhaAgent;
import hospital.enums.Local;
import jade.core.AID;

import java.util.ArrayList;
import java.util.List;

public class Bairro {
    private final Local[][] mapa;
    private final int[] posicaoHospital; // posição fixa do único hospital

    // Referência ao agente hospital (opcional, útil para consultas)
    private HospitalDeCampanhaAgent hospitalAgente;

    // Listas separadas para cada tipo de agente
    private final List<ChildAgent> todosChild = new ArrayList<>();
    private final List<AdultAgent> todosAdult = new ArrayList<>();
    private final List<ElderAgent> todosElder = new ArrayList<>();

    public Bairro() {
        // Mapa 4x4 com um hospital fixo
        mapa = new Local[4][4];

        // Preenchendo o mapa com locais
        mapa[0][0] = Local.CASA;
        mapa[0][1] = Local.ESCOLA;
        mapa[0][2] = Local.PARQUE;
        mapa[0][3] = Local.HOSPITAL; // ÚNICO HOSPITAL

        mapa[1][0] = Local.PARQUE;
        mapa[1][1] = Local.ATIVIDADE;
        mapa[1][2] = Local.ESCOLA;
        mapa[1][3] = Local.CASA;

        mapa[2][0] = Local.CASA;
        mapa[2][1] = Local.ATIVIDADE;
        mapa[2][2] = Local.PARQUE;
        mapa[2][3] = Local.ESCOLA;

        mapa[3][0] = Local.CASA; // futuramente FESTA
        mapa[3][1] = Local.ATIVIDADE;
        mapa[3][2] = Local.PARQUE;
        mapa[3][3] = Local.CASA;

        // Define a posição do hospital
        posicaoHospital = new int[]{0, 3};
    }

    // ====================== HOSPITAL ======================
    public HospitalDeCampanhaAgent getHospitalAgente() {
        return hospitalAgente;
    }

    public void setHospitalAgente(HospitalDeCampanhaAgent hospitalAgente) {
        this.hospitalAgente = hospitalAgente;
    }

    // ====================== MAPA ======================

    public Local getLocal(int x, int y) {
        if (x >= 0 && x < mapa.length && y >= 0 && y < mapa[0].length) {
            return mapa[x][y];
        }
        return null;
    }

    public int getLinhas() {
        return mapa.length;
    }

    public int getColunas() {
        return mapa[0].length;
    }

    // Retorna a posição fixa do hospital
    public int[] getHospitalPos() {
        return posicaoHospital;
    }

    // Verifica se o hospital está cheio
    public boolean isHospitalLotado(List<AID> internados, int capacidade) {
        return internados.size() >= capacidade;
    }

    // ====================== AGENTES ======================
    public void adicionarAgenteChild(ChildAgent agente) {
        todosChild.add(agente);
    }

    public void adicionarAgenteAdult(AdultAgent agente) {
        todosAdult.add(agente);
    }

    public void adicionarAgenteElder(ElderAgent agente) {
        todosElder.add(agente);
    }

    public List<ChildAgent> getTodosChild() {
        return todosChild;
    }

    public List<AdultAgent> getTodosAdult() {
        return todosAdult;
    }

    public List<ElderAgent> getTodosElder() {
        return todosElder;
    }

    // Retorna todos os agentes em uma célula (x, y)
    public List<Object> getTodosAgentesNoLocal(int x, int y) {
        List<Object> lista = new ArrayList<>();
        lista.addAll(getAgentesNoLocalChild(x, y));
        lista.addAll(getAgentesNoLocalAdult(x, y));
        lista.addAll(getAgentesNoLocalElder(x, y));
        return lista;
    }

    public List<ChildAgent> getAgentesNoLocalChild(int x, int y) {
        List<ChildAgent> lista = new ArrayList<>();
        for (ChildAgent agente : todosChild) {
            if (agente.getPosX() == x && agente.getPosY() == y) {
                lista.add(agente);
            }
        }
        return lista;
    }

    public List<AdultAgent> getAgentesNoLocalAdult(int x, int y) {
        List<AdultAgent> lista = new ArrayList<>();
        for (AdultAgent agente : todosAdult) {
            if (agente.getPosX() == x && agente.getPosY() == y) {
                lista.add(agente);
            }
        }
        return lista;
    }

    public List<ElderAgent> getAgentesNoLocalElder(int x, int y) {
        List<ElderAgent> lista = new ArrayList<>();
        for (ElderAgent agente : todosElder) {
            if (agente.getPosX() == x && agente.getPosY() == y) {
                lista.add(agente);
            }
        }
        return lista;
    }

    // ====================== VISUALIZAÇÃO ======================

    public void imprimirEstado(int tick) {
        System.out.println("\n📅 Tick " + tick + ":");

        for (int i = 0; i < getLinhas(); i++) {
            for (int j = 0; j < getColunas(); j++) {
                List<Object> agentesAqui = getTodosAgentesNoLocal(i, j);
                StringBuilder celula = new StringBuilder("[ ");

                // Mostra o símbolo do hospital
                if (mapa[i][j] == Local.HOSPITAL) {
                    celula.append("🏥 ");
                }

                for (Object a : agentesAqui) {
                    String nome;
                    boolean infectado = false;
                    boolean hospitalizado = false;

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

                    if (hospitalizado) {
                        celula.append("🏥").append(nome).append(", ");
                    } else if (infectado) {
                        celula.append("💀").append(nome).append(", ");
                    } else {
                        celula.append(nome).append(", ");
                    }
                }

                if (!agentesAqui.isEmpty()) celula.setLength(celula.length() - 2);
                celula.append(" ] ");
                System.out.print(celula);
            }
            System.out.println();
        }

        // Estatísticas gerais
        long totalInfectados = todosChild.stream().filter(ChildAgent::isInfectado).count()
                + todosAdult.stream().filter(AdultAgent::isInfectado).count()
                + todosElder.stream().filter(ElderAgent::isInfectado).count();

        long totalAgentes = todosChild.size() + todosAdult.size() + todosElder.size();

        System.out.println("\n📊 Estatísticas: Infectados = " + totalInfectados +
                " | Total de agentes = " + totalAgentes +
                " | Hospital em (" + posicaoHospital[0] + "," + posicaoHospital[1] + ")");
    }
}
