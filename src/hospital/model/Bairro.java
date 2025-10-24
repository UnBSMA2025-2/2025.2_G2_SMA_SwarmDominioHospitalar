package hospital.model;

import hospital.agents.ChildAgent;
import hospital.enums.Local;

import java.util.ArrayList;
import java.util.List;

public class Bairro {
    private final Local[][] mapa;

    // Lista de todos os agentes
    private final List<ChildAgent> todosAgentes = new ArrayList<>();

    public Bairro() {
        // Exemplo simples 3x3
        mapa = new Local[3][3];

        // Preenchendo o mapa
        mapa[0][0] = Local.CASA;
        mapa[0][1] = Local.ESCOLA;
        mapa[0][2] = Local.PARQUE;

        mapa[1][0] = Local.PARQUE;
        mapa[1][1] = Local.ATIVIDADE;
        mapa[1][2] = Local.ESCOLA;

        mapa[2][0] = Local.CASA;
        mapa[2][1] = Local.ATIVIDADE;
        mapa[2][2] = Local.PARQUE;
    }

    // ====================== MÉTODOS DO MAPA ======================
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

    // ====================== MÉTODOS DE AGENTES ======================
    public void adicionarAgente(ChildAgent agente) {
        todosAgentes.add(agente);
    }

    public List<ChildAgent> getTodosAgentes() {
        return todosAgentes;
    }

    // Retorna os agentes na célula (x,y)
    public List<ChildAgent> getAgentesNoLocal(int x, int y) {
        List<ChildAgent> lista = new ArrayList<>();
        for (ChildAgent agente : todosAgentes) {
            if (agente.getPosX() == x && agente.getPosY() == y) {
                lista.add(agente);
            }
        }
        return lista;
    }
}
