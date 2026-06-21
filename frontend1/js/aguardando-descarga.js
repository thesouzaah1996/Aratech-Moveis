const LIMITE_ESPERA_MINUTOS = 60;

let fila = [
    {
        empresa: "Madeiras União",
        produto: "MDF 15mm",
        chegada: new Date(Date.now() - 30 * 60000),
        motorista: "Carlos Silva",
        status: "Aguardando"
    },
    {
        empresa: "Trans Horizonte",
        produto: "Ferragens",
        chegada: new Date(Date.now() - 90 * 60000),
        motorista: "João Mendes",
        status: "Aguardando"
    }
];

let historico = [];
let indexAtual = null;

document.addEventListener("DOMContentLoaded", () => {
    verificarTempoEspera();
    renderFila();
    setInterval(verificarTempoEspera, 60000);
});

function verificarTempoEspera() {
    const agora = new Date();

    fila.forEach(item => {

        if (item.status === "Descarregando") return;

        const diffMin = (agora - new Date(item.chegada)) / 60000;

        if (diffMin >= LIMITE_ESPERA_MINUTOS) {
            item.status = "Aguardando há mais de 1 hora";
        } else {
            item.status = "Aguardando";
        }
    });

    renderFila();
}

function renderFila() {
    const tbody = document.getElementById("filaTable");

    tbody.innerHTML = fila.map((c, index) => {

        let badgeClass = "bg-primary";
        let rowClass = "";

        if (c.status.includes("1 hora")) {
            badgeClass = "bg-danger";
            rowClass = "table-danger";
        }

        if (c.status === "Descarregando") {
            badgeClass = "bg-info text-dark";
        }

        return `
        <tr class="${rowClass}">
            <td class="fw-semibold">${c.empresa}</td>
            <td>${c.produto}</td>
            <td>${formatarHora(c.chegada)}</td>
            <td>${c.motorista}</td>
            <td>
                <span class="badge ${badgeClass}">
                    ${c.status}
                </span>
            </td>
            <td class="text-end">
                <div class="d-flex justify-content-end gap-2">

                    ${c.status !== "Descarregando" ? `
                        <button class="btn btn-primary btn-sm px-3"
                            onclick="abrirModalLiberar(${index})">
                            <i class="bi bi-box-arrow-in-right me-1"></i>
                            Liberar
                        </button>
                    ` : ``}

                    <button class="btn btn-success btn-sm px-3"
                        onclick="abrirModalFinalizar(${index})">
                        <i class="bi bi-check-circle me-1"></i>
                        Finalizar
                    </button>

                </div>
            </td>
        </tr>
        `;
    }).join('');
}

function abrirModalFinalizar(index) {
    indexAtual = index;
    document.getElementById("modalObservacao").value = "";
    document.getElementById("modalDependencia").checked = false;

    new bootstrap.Modal(
        document.getElementById('finalizarModal')
    ).show();
}

function confirmarFinalizacao() {
    const obs = document.getElementById("modalObservacao").value.trim();
    const dependencia = document.getElementById("modalDependencia").checked;

    if (!obs) {
        alert("Observações são obrigatórias.");
        return;
    }

    const item = fila[indexAtual];

    item.status = dependencia ? "Dependência" : "Finalizado";
    item.data = new Date().toLocaleDateString();
    item.obs = obs;

    historico.push(item);
    fila.splice(indexAtual, 1);

    bootstrap.Modal.getInstance(
        document.getElementById('finalizarModal')
    ).hide();

    renderFila();
    showSuccess("Entrega finalizada com sucesso.");
}

function abrirModalLiberar(index) {
    indexAtual = index;

    new bootstrap.Modal(
        document.getElementById('liberarModal')
    ).show();
}

function confirmarLiberacao() {
    fila[indexAtual].status = "Descarregando";

    bootstrap.Modal.getInstance(
        document.getElementById('liberarModal')
    ).hide();

    renderFila();
    showSuccess("Liberado para descarregamento.");
}

function formatarHora(data) {
    return new Date(data).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
    });
}

function showSuccess(msg) {
    const container = document.getElementById('alertContainer');
    container.innerHTML = `
        <div class="alert alert-success alert-toast">
            <i class="bi bi-check-circle-fill me-2"></i>
            ${msg}
        </div>
    `;
    setTimeout(() => container.innerHTML = '', 3000);
}