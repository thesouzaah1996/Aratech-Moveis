// =======================
// Dados Chumbados
// =======================
let estoque = [
    { codigo: "PRD001", produto: "Porta de Guarda-Roupa", quantidade: 50 },
    { codigo: "PRD002", produto: "Puxador", quantidade: 200 },
    { codigo: "PRD003", produto: "Dobradiça", quantidade: 120 },
    { codigo: "PRD004", produto: "Corrediça", quantidade: 80 }
];

let lotes = [
    {
        id: 101,
        cliente: "Casas Bahia - Porto Alegre",
        itens: [
            { produto: "Porta de Guarda-Roupa", quantidade: 2 },
            { produto: "Puxador", quantidade: 20 }
        ],
        status: "CRIADO",
        data: "08/04/2026"
    }
];

let itensLote = [];

// =======================
// Renderizar Estoque
// =======================
function renderEstoque() {
    const tbody = document.getElementById("estoqueTable");
    tbody.innerHTML = "";

    estoque.forEach(item => {
        tbody.innerHTML += `
            <tr>
                <td>${item.codigo}</td>
                <td>${item.produto}</td>
                <td><span class="badge bg-success">${item.quantidade}</span></td>
            </tr>
        `;
    });
}

// =======================
// Adicionar Item ao Lote
// =======================
function adicionarItemLote() {
    const produto = document.getElementById("produtoLote").value;
    const quantidade = document.getElementById("quantidadeLote").value;

    if (!produto || quantidade <= 0) return;

    itensLote.push({ produto, quantidade });
    renderItensLote();
    document.getElementById("quantidadeLote").value = "";
}

// =======================
// Renderizar Itens do Lote
// =======================
function renderItensLote() {
    const tbody = document.getElementById("itensLoteTable");
    tbody.innerHTML = "";

    itensLote.forEach((item, index) => {
        tbody.innerHTML += `
            <tr>
                <td>${item.produto}</td>
                <td>${item.quantidade}</td>
                <td>
                    <button class="btn btn-sm btn-danger" onclick="removerItem(${index})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    });
}

function removerItem(index) {
    itensLote.splice(index, 1);
    renderItensLote();
}

// =======================
// Criar Lote
// =======================
document.getElementById("loteForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const cliente = document.getElementById("cliente").value;
    const observacao = document.getElementById("observacaoLote").value;

    const novoLote = {
        id: Math.floor(Math.random() * 1000),
        cliente,
        itens: [...itensLote],
        status: "CRIADO",
        data: new Date().toLocaleDateString()
    };

    lotes.push(novoLote);
    itensLote = [];
    renderItensLote();
    renderLotes();
    this.reset();
});

// =======================
// Renderizar Lotes
// =======================
function renderLotes() {
    const tbody = document.getElementById("lotesTable");
    tbody.innerHTML = "";

    lotes.forEach(lote => {
        const itensDescricao = lote.itens
            .map(i => `${i.quantidade}x ${i.produto}`)
            .join(", ");

        tbody.innerHTML += `
            <tr>
                <td>${lote.id}</td>
                <td>${lote.cliente}</td>
                <td>${itensDescricao}</td>
                <td>${getStatusBadge(lote.status)}</td>
                <td>${lote.data}</td>
                <td class="text-end">
                    <button class="btn btn-sm btn-success" onclick="avancarStatus(${lote.id})">
                        <i class="bi bi-arrow-right-circle"></i>
                    </button>
                </td>
            </tr>
        `;
    });
}

// =======================
// Badge de Status
// =======================
function getStatusBadge(status) {
    const classes = {
        CRIADO: "status-criado",
        EM_SEPARACAO: "status-separacao",
        SEPARADO: "status-separado",
        LIBERADO_PARA_EMBALAGEM: "status-embalagem",
        EMBALADO: "status-embalado",
        LIBERADO_PARA_CARREGAMENTO: "status-carregamento"
    };

    return `<span class="badge badge-status ${classes[status]}">${status.replaceAll("_", " ")}</span>`;
}

// =======================
// Avançar Status
// =======================
function avancarStatus(id) {
    const fluxo = [
        "CRIADO",
        "EM_SEPARACAO",
        "SEPARADO",
        "LIBERADO_PARA_EMBALAGEM",
        "EMBALADO",
        "LIBERADO_PARA_CARREGAMENTO"
    ];

    const lote = lotes.find(l => l.id === id);
    const index = fluxo.indexOf(lote.status);

    if (index < fluxo.length - 1) {
        lote.status = fluxo[index + 1];
        renderLotes();
    }
}

// =======================
// Inicialização
// =======================
document.addEventListener("DOMContentLoaded", () => {
    renderEstoque();
    renderLotes();
});