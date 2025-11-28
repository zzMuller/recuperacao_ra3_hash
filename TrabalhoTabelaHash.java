import java.util.Random;

public class TrabalhoTabelaHash {

    public static final int TAMANHO_TABELA_PEQUENA = 1009;
    public static final int TAMANHO_TABELA_MEDIA   = 10007;
    public static final int TAMANHO_TABELA_GRANDE  = 100003;

    public static final int TAMANHO_DATASET_PEQUENO = 1000;
    public static final int TAMANHO_DATASET_MEDIO   = 10000;
    public static final int TAMANHO_DATASET_GRANDE  = 100000;

    public static final long SEED_PEQUENO = 137L;
    public static final long SEED_MEDIO   = 271828L;
    public static final long SEED_GRANDE  = 314159L;

    public static final int REPETICOES = 5;

    public static void main(String[] args) {
        imprimirCabecalhoCsv();
        rodarExperimentos();
    }

    public static void imprimirCabecalhoCsv() {
        System.out.println(
            "m,n,func,seed," +
            "ins_ms,coll_tbl,coll_lst," +
            "find_ms_hits,find_ms_misses," +
            "cmp_hits,cmp_misses,checksum"
        );
    }

    public static void rodarExperimentos() {
        int[] tamanhosTabela     = {TAMANHO_TABELA_PEQUENA, TAMANHO_TABELA_MEDIA, TAMANHO_TABELA_GRANDE};
        int[] tamanhosDataset    = {TAMANHO_DATASET_PEQUENO, TAMANHO_DATASET_MEDIO, TAMANHO_DATASET_GRANDE};
        long[] seedsParaDatasets = {SEED_PEQUENO, SEED_MEDIO, SEED_GRANDE};

        int indiceTamanhoDataset = 0;
        while (indiceTamanhoDataset < 3) {
            int quantidadeDeRegistros = tamanhosDataset[indiceTamanhoDataset];
            long seedAtual            = seedsParaDatasets[indiceTamanhoDataset];

            int indiceTamanhoTabela = 0;
            while (indiceTamanhoTabela < 3) {
                int tamanhoTabela = tamanhosTabela[indiceTamanhoTabela];

                rodarExperimentosParaTamanho(tamanhoTabela, quantidadeDeRegistros, seedAtual);

                indiceTamanhoTabela = indiceTamanhoTabela + 1;
            }

            indiceTamanhoDataset = indiceTamanhoDataset + 1;
        }
    }

    public static void rodarExperimentosParaTamanho(
        int tamanhoTabela,
        int quantidadeDeRegistros,
        long seed
    ) {
        int[] codigosInsercao = new int[quantidadeDeRegistros];
        GeradorDados.preencherDados(codigosInsercao, quantidadeDeRegistros, seed);

        int tamanhoLoteBusca   = quantidadeDeRegistros;
        int metadeLoteBusca    = tamanhoLoteBusca / 2;
        int[] codigosParaBusca = new int[tamanhoLoteBusca];

        int posicao = 0;
        while (posicao < metadeLoteBusca) {
            codigosParaBusca[posicao] = codigosInsercao[posicao];
            posicao = posicao + 1;
        }

        GeradorDados.preencherChavesAusentes(
            codigosParaBusca,
            metadeLoteBusca,
            tamanhoLoteBusca,
            seed + 1
        );

        rodarExperimentoUmaFuncao(
            tamanhoTabela,
            quantidadeDeRegistros,
            seed,
            codigosInsercao,
            codigosParaBusca,
            TabelaHashEncadeada.TIPO_HASH_DIVISAO
        );

        rodarExperimentoUmaFuncao(
            tamanhoTabela,
            quantidadeDeRegistros,
            seed,
            codigosInsercao,
            codigosParaBusca,
            TabelaHashEncadeada.TIPO_HASH_MULTIPLICACAO
        );

        rodarExperimentoUmaFuncao(
            tamanhoTabela,
            quantidadeDeRegistros,
            seed,
            codigosInsercao,
            codigosParaBusca,
            TabelaHashEncadeada.TIPO_HASH_DOBRAMENTO
        );
    }

    public static void rodarExperimentoUmaFuncao(
        int tamanhoTabela,
        int quantidadeDeRegistros,
        long seed,
        int[] codigosInsercao,
        int[] codigosParaBusca,
        int tipoFuncaoHash
    ) {
        long somaTempoInsercaoMs          = 0;
        long somaColisoesTabela           = 0;
        long somaColisoesLista            = 0;
        long somaTempoBuscasAcertosMs     = 0;
        long somaTempoBuscasErrosMs       = 0;
        long somaComparacoesBuscasAcertos = 0;
        long somaComparacoesBuscasErros   = 0;

        int checasomaFinal = 0;

        int numeroRepeticao = 0;
        while (numeroRepeticao < REPETICOES) {

            if (tipoFuncaoHash == TabelaHashEncadeada.TIPO_HASH_DIVISAO) {
                System.err.println("H_DIV " + tamanhoTabela + " " + seed);
            } else {
                if (tipoFuncaoHash == TabelaHashEncadeada.TIPO_HASH_MULTIPLICACAO) {
                    System.err.println("H_MUL " + tamanhoTabela + " " + seed);
                } else {
                    if (tipoFuncaoHash == TabelaHashEncadeada.TIPO_HASH_DOBRAMENTO) {
                        System.err.println("H_FOLD " + tamanhoTabela + " " + seed);
                    }
                }
            }


            TabelaHashEncadeada tabela =
                new TabelaHashEncadeada(tamanhoTabela, tipoFuncaoHash);

            long instanteInicioInsercao = System.nanoTime();

            int posicaoInsercao = 0;
            while (posicaoInsercao < quantidadeDeRegistros) {
                Registro registroAtual = new Registro(codigosInsercao[posicaoInsercao]);
                tabela.inserir(registroAtual);
                posicaoInsercao = posicaoInsercao + 1;
            }

            long instanteFimInsercao = System.nanoTime();

            long tempoInsercaoMs = (instanteFimInsercao - instanteInicioInsercao) / 1000000L;
            somaTempoInsercaoMs  = somaTempoInsercaoMs + tempoInsercaoMs;

            somaColisoesTabela = somaColisoesTabela + tabela.colisoesTabela;
            somaColisoesLista  = somaColisoesLista + tabela.colisoesListaInsercao;

            checasomaFinal = tabela.calcularChecasum();

            int metadeQuantidadeRegistros = quantidadeDeRegistros / 2;

            ContadorComparacoes contadorAcertos = new ContadorComparacoes();
            ContadorComparacoes contadorErros   = new ContadorComparacoes();

            long instanteInicioBuscasAcertos = System.nanoTime();
            int posicaoBusca = 0;
            while (posicaoBusca < metadeQuantidadeRegistros) {
                tabela.buscar(codigosParaBusca[posicaoBusca], contadorAcertos);
                posicaoBusca = posicaoBusca + 1;
            }
            long instanteFimBuscasAcertos = System.nanoTime();

            long instanteInicioBuscasErros = System.nanoTime();
            posicaoBusca = metadeQuantidadeRegistros;
            while (posicaoBusca < quantidadeDeRegistros) {
                tabela.buscar(codigosParaBusca[posicaoBusca], contadorErros);
                posicaoBusca = posicaoBusca + 1;
            }
            long instanteFimBuscasErros = System.nanoTime();

            long tempoBuscasAcertosMs = (instanteFimBuscasAcertos - instanteInicioBuscasAcertos) / 1000000L;
            long tempoBuscasErrosMs   = (instanteFimBuscasErros   - instanteInicioBuscasErros)   / 1000000L;

            somaTempoBuscasAcertosMs = somaTempoBuscasAcertosMs + tempoBuscasAcertosMs;
            somaTempoBuscasErrosMs   = somaTempoBuscasErrosMs   + tempoBuscasErrosMs;

            somaComparacoesBuscasAcertos = somaComparacoesBuscasAcertos + contadorAcertos.comparacoes;
            somaComparacoesBuscasErros   = somaComparacoesBuscasErros   + contadorErros.comparacoes;

            numeroRepeticao = numeroRepeticao + 1;
        }

        long mediaTempoInsercaoMs        = somaTempoInsercaoMs          / REPETICOES;
        long mediaColisoesTabela         = somaColisoesTabela           / REPETICOES;
        long mediaColisoesLista          = somaColisoesLista            / REPETICOES;
        long mediaTempoBuscasAcertosMs   = somaTempoBuscasAcertosMs     / REPETICOES;
        long mediaTempoBuscasErrosMs     = somaTempoBuscasErrosMs       / REPETICOES;
        long mediaComparacoesBuscasHits  = somaComparacoesBuscasAcertos / REPETICOES;
        long mediaComparacoesBuscasMiss  = somaComparacoesBuscasErros   / REPETICOES;

        String nomeFuncaoHash = "H_DIV";
        if (tipoFuncaoHash == TabelaHashEncadeada.TIPO_HASH_MULTIPLICACAO) {
            nomeFuncaoHash = "H_MUL";
        } else if (tipoFuncaoHash == TabelaHashEncadeada.TIPO_HASH_DOBRAMENTO) {
            nomeFuncaoHash = "H_FOLD";
        }

        System.out.println(
            tamanhoTabela + "," +
            quantidadeDeRegistros + "," +
            nomeFuncaoHash + "," +
            seed + "," +
            mediaTempoInsercaoMs + "," +
            mediaColisoesTabela + "," +
            mediaColisoesLista + "," +
            mediaTempoBuscasAcertosMs + "," +
            mediaTempoBuscasErrosMs + "," +
            mediaComparacoesBuscasHits + "," +
            mediaComparacoesBuscasMiss + "," +
            checasomaFinal
        );
    }
}

class Registro {
    public int codigo;

    public Registro(int codigo) {
        this.codigo = codigo;
    }
}

class NoLista {
    public Registro registro;
    public NoLista proximo;

    public NoLista(Registro registro) {
        this.registro = registro;
        this.proximo  = null;
    }
}

class ListaEncadeada {
    public NoLista primeiro;
    public NoLista ultimo;
    public int quantidade;

    public ListaEncadeada() {
        this.primeiro   = null;
        this.ultimo     = null;
        this.quantidade = 0;
    }

    public int estaVazia() {
        if (primeiro == null) {
            return 1;
        } else {
            return 0;
        }
    }

    public int inserirNoFinalComContagem(Registro registro) {
        NoLista novo = new NoLista(registro);
        int colisoesLista = 0;

        if (primeiro == null) {
            primeiro   = novo;
            ultimo     = novo;
            quantidade = quantidade + 1;
            return colisoesLista;
        }

        NoLista atual = primeiro;
        while (atual.proximo != null) {
            colisoesLista = colisoesLista + 1;
            atual = atual.proximo;
        }

        atual.proximo = novo;
        ultimo        = novo;
        quantidade    = quantidade + 1;

        return colisoesLista;
    }
}

class ContadorComparacoes {
    public long comparacoes;

    public ContadorComparacoes() {
        this.comparacoes = 0;
    }
}

class TabelaHashEncadeada {

    public static final int TIPO_HASH_DIVISAO        = 1;
    public static final int TIPO_HASH_MULTIPLICACAO  = 2;
    public static final int TIPO_HASH_DOBRAMENTO     = 3;

    public int tamanhoTabela;
    public int tipoFuncaoHash;
    public ListaEncadeada[] tabela;

    public long colisoesTabela;
    public long colisoesListaInsercao;

    public int[] primeirosHash;
    public int quantidadeHashGuardados;

    public static final double FATOR_MULTIPLICACAO_HASH = 0.6180339887;

    public TabelaHashEncadeada(int tamanhoTabela, int tipoFuncaoHash) {
        this.tamanhoTabela  = tamanhoTabela;
        this.tipoFuncaoHash = tipoFuncaoHash;
        this.tabela         = new ListaEncadeada[tamanhoTabela];

        int indice = 0;
        while (indice < tamanhoTabela) {
            tabela[indice] = new ListaEncadeada();
            indice = indice + 1;
        }

        this.colisoesTabela          = 0;
        this.colisoesListaInsercao   = 0;
        this.primeirosHash           = new int[10];
        this.quantidadeHashGuardados = 0;
    }

    public void inserir(Registro registro) {
        int indice = calcularIndice(registro.codigo);

        ListaEncadeada listaNoCompartimento = tabela[indice];

        if (listaNoCompartimento.estaVazia() == 0) {
            colisoesTabela = colisoesTabela + 1;
        }

        int colisoesLista = listaNoCompartimento.inserirNoFinalComContagem(registro);
        colisoesListaInsercao = colisoesListaInsercao + colisoesLista;

        guardarPrimeirosHash(indice);
    }

    public int buscar(int chave, ContadorComparacoes contador) {
        int indice = calcularIndice(chave);
        ListaEncadeada listaNoCompartimento = tabela[indice];

        NoLista atual = listaNoCompartimento.primeiro;
        while (atual != null) {
            contador.comparacoes = contador.comparacoes + 1;

            if (atual.registro.codigo == chave) {
                return 1;
            }

            atual = atual.proximo;
        }

        return 0;
    }

    public int calcularIndice(int chave) {
        int indice = 0;

        if (tipoFuncaoHash == TIPO_HASH_DIVISAO) {
            indice = funcaoDivisao(chave);
        } else if (tipoFuncaoHash == TIPO_HASH_MULTIPLICACAO) {
            indice = funcaoMultiplicacao(chave);
        } else if (tipoFuncaoHash == TIPO_HASH_DOBRAMENTO) {
            indice = funcaoDobramento(chave);
        }

        if (indice < 0) {
            indice = indice * -1;
        }

        if (indice >= tamanhoTabela) {
            indice = indice % tamanhoTabela;
        }

        return indice;
    }

    public int funcaoDivisao(int chave) {
        int resto = chave % tamanhoTabela;
        if (resto < 0) {
            resto = resto + tamanhoTabela;
        }
        return resto;
    }

    public int funcaoMultiplicacao(int chave) {
        double valor = chave * FATOR_MULTIPLICACAO_HASH;
        int parteInteira = (int) valor;
        double parteFracionaria = valor - parteInteira;
        double produto = tamanhoTabela * parteFracionaria;
        int indice = (int) produto;
        return indice;
    }

    public int funcaoDobramento(int chave) {
        int valor = chave;
        int soma  = 0;

        while (valor > 0) {
            int bloco = valor % 1000;
            soma = soma + bloco;
            valor = valor / 1000;
        }

        int indice = soma % tamanhoTabela;
        if (indice < 0) {
            indice = indice + tamanhoTabela;
        }
        return indice;
    }

    public void guardarPrimeirosHash(int valorHash) {
        if (quantidadeHashGuardados < 10) {
            primeirosHash[quantidadeHashGuardados] = valorHash;
            quantidadeHashGuardados = quantidadeHashGuardados + 1;
        }
    }

    public int calcularChecasum() {
        long soma = 0;
        int indice = 0;
        while (indice < quantidadeHashGuardados) {
            soma = soma + primeirosHash[indice];
            indice = indice + 1;
        }
        long restoLong = soma % 1000003L;
        int resto = (int) restoLong;
        if (resto < 0) {
            resto = resto * -1;
        }
        return resto;
    }
}

class GeradorDados {

    public static void preencherDados(int[] dados, int quantidade, long seed) {
        Random gerador = new Random(seed);
        int inicio    = 100000000;
        int intervalo = 900000000;

        int indice = 0;
        while (indice < quantidade) {
            int valor = gerador.nextInt(intervalo);
            valor = valor + inicio;
            dados[indice] = valor;
            indice = indice + 1;
        }
    }

    public static void preencherChavesAusentes(
        int[] destino,
        int inicio,
        int fim,
        long seed
    ) {
        Random gerador = new Random(seed);
        int minimo    = 100000000;
        int intervalo = 900000000;

        int indice = inicio;
        while (indice < fim) {
            int valor = gerador.nextInt(intervalo);
            valor = valor + minimo;
            destino[indice] = valor;
            indice = indice + 1;
        }
    }
}
