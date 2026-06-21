const API_URL = 'http://localhost:8080/portaria';

let queue = [];
let historico = [];
let removeId = null;
let confirmId = null;

async function fetchQueue() {
    try {
        const response = await fetch(`${API_URL}/all`);
        const result = await response.json();

        queue = (result.data || []).map(item => ({
            id: item.id,
            notaFiscal: item.notaFiscal,
            empresa: item.empresa,
            motorista: item.motorista,
            placa: item.placa,
            setor: item.setor,
            status: item.status,
            timestamp: new Date(item.dataEntrada || Date.now()).getTime()
        }));

        renderQueue();
    } catch (error) {
        console.error(error);
        showAlert('Erro ao carregar dados.', 'danger');
    }
}

function renderQueue() {
    const tbody = document.getElementById('queueTable');
    const emptyMessage = document.getElementById('queueEmptyMessage');

    if (!tbody) return;

    if (queue.length === 0) {
        tbody.innerHTML = '';
        if (emptyMessage) emptyMessage.style.display = 'block';
        return;
    }

    if (emptyMessage) emptyMessage.style.display = 'none';

    let html = '';

    queue.forEach(item => {

        let primaryButton = '';

        if (item.status === 'AGUARDANDO') {
            primaryButton = `
                <button class="btn btn-sm btn-secondary me-1" disabled>
                    <i class="bi bi-hourglass"></i> Aguardando
                </button>
            `;
        }

        if (item.status === 'AUTORIZADO') {
            primaryButton = `
                <button class="btn btn-sm btn-primary me-1">
                    <i class="bi bi-truck"></i> Autorizado
                </button>
            `;
        }

        const actionsHtml = `
            ${primaryButton}

            <button class="btn btn-sm btn-success me-1"
                ${item.status !== 'AUTORIZADO' ? 'disabled' : ''}
                onclick="openConfirmModal(${item.id})">
                <i class="bi bi-check-circle"></i>
            </button>

            <button class="btn btn-sm btn-danger"
                onclick="openRemoveModal(${item.id})">
                <i class="bi bi-x-circle"></i>
            </button>
        `;

        html += `
            <tr>
                <td>${item.notaFiscal}</td>
                <td>${item.empresa}</td>
                <td>${item.motorista}</td>
                <td>${item.placa}</td>
                <td>${item.setor}</td>
                <td>${formatWaitTime(item.timestamp)}</td>
                <td class="text-end">${actionsHtml}</td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

async function handleCheckinSubmit(e) {
    e.preventDefault();

    const notaFiscal = document.getElementById('notaFiscal').value.trim();
    const empresa = document.getElementById('empresa').value.trim();
    const motorista = document.getElementById('motorista').value.trim();
    const placa = document.getElementById('placa').value.trim();
    const setor = document.getElementById('setor').value;

    if (!notaFiscal || !empresa || !motorista || !placa || !setor) {
        showAlert('Preencha todos os campos.', 'danger');
        return;
    }

    try {
        await fetch(`${API_URL}/save`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                notaFiscal,
                empresa,
                motorista,
                placa,
                setor
            })
        });

        showAlert('Caminhão registrado.', 'success');
        e.target.reset();
        fetchQueue();

    } catch (error) {
        console.error(error);
        showAlert('Erro ao salvar.', 'danger');
    }
}

function openConfirmModal(id) {
    confirmId = id;
    const modal = new bootstrap.Modal(document.getElementById('confirmEntradaModal'));
    modal.show();
}

function confirmarEntrada() {
    // Aqui depende de endpoint que vc ainda não tem (tipo update status)
    showAlert('Endpoint de confirmação ainda não implementado.', 'warning');

    const modal = bootstrap.Modal.getInstance(document.getElementById('confirmEntradaModal'));
    modal.hide();
}

function openRemoveModal(id) {
    removeId = id;
    const modal = new bootstrap.Modal(document.getElementById('removeConfirmModal'));
    modal.show();
}

async function confirmRemove() {
    if (!removeId) return;

    try {
        await fetch(`${API_URL}/delete/${removeId}`, {
            method: 'DELETE'
        });

        showAlert('Removido com sucesso.', 'success');
        fetchQueue();

    } catch (error) {
        console.error(error);
        showAlert('Erro ao remover.', 'danger');
    }

    removeId = null;

    const modal = bootstrap.Modal.getInstance(document.getElementById('removeConfirmModal'));
    modal.hide();
}

function formatWaitTime(timestamp) {
    const diff = Date.now() - timestamp;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (hours > 0) return `${hours}h ${remainingMinutes}min`;
    return `${minutes} min`;
}

function showAlert(message, type = 'success') {
    const container = document.getElementById('alertContainer');
    if (!container) return;

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 end-0 m-3`;
    alertDiv.style.zIndex = '9999';

    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    container.appendChild(alertDiv);
    setTimeout(() => alertDiv.remove(), 4000);
}

document.addEventListener('DOMContentLoaded', () => {
    fetchQueue();

    const form = document.getElementById('checkinForm');
    if (form) {
        form.addEventListener('submit', handleCheckinSubmit);
    }
});