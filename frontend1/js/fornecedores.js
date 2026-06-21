const API_BASE_URL = 'http://localhost:8091/almoxarifado/fornecedor';
const ITEMS_PER_PAGE = 10;

let supplierModal;
let deleteModal;
let supplierToDelete = null;

let allSuppliers = [];
let currentPage = 1;
let totalPages = 1;

document.addEventListener('DOMContentLoaded', function() {
    if (typeof bootstrap !== 'undefined') {
        const modalElement = document.getElementById('supplierModal');
        if (modalElement) {
            supplierModal = new bootstrap.Modal(modalElement);
        }

        const deleteModalElement = document.getElementById('deleteConfirmModal');
        if (deleteModalElement) {
            deleteModal = new bootstrap.Modal(deleteModalElement);
        }
    }

    loadSuppliers();
    applyPhoneMask('telefone', 10);
    applyPhoneMask('telefoneRepresentante', 11);
});

function applyPhoneMask(fieldId, maxDigits) {
    const input = document.getElementById(fieldId);
    if (!input) return;
    input.addEventListener('input', function(e) {
        let digits = e.target.value.replace(/\D/g, '').slice(0, maxDigits);
        const isMobile = maxDigits === 11;

        if (digits.length > (isMobile ? 7 : 6)) {
            const mid = isMobile ? 5 : 4;
            e.target.value = digits.replace(new RegExp(`^(\\d{2})(\\d{${mid}})(\\d{0,4})`), '($1) $2-$3');
        } else if (digits.length > 2) {
            e.target.value = digits.replace(/^(\d{2})(\d{0,5})/, '($1) $2');
        } else if (digits.length > 0) {
            e.target.value = '(' + digits;
        }
    });
}

async function loadSuppliers() {
    try {
        const response = await fetch(`${API_BASE_URL}/all`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        const data = await response.json();

        if (data.fornecedores && Array.isArray(data.fornecedores)) {
            allSuppliers = data.fornecedores;
            totalPages = Math.ceil(allSuppliers.length / ITEMS_PER_PAGE);
            renderCurrentPage();
            renderPagination();
        } else {
            showError('Nenhum fornecedor encontrado');
        }
    } catch (error) {
        showError('Erro ao carregar fornecedores');
    }
}

function renderCurrentPage() {
    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    const endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allSuppliers.length);
    const pageSuppliers = allSuppliers.slice(startIndex, endIndex);

    renderSupplierTable(pageSuppliers);
    updatePaginationInfo(startIndex, endIndex);
}

function renderSupplierTable(suppliers) {
    const tbody = document.getElementById('supplierTable');

    if (!suppliers || suppliers.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center py-5 text-muted">
                    <i class="bi bi-building fs-1 d-block mb-3 opacity-50"></i>
                    <h5 class="fw-normal">Nenhum fornecedor cadastrado</h5>
                    <p class="small mb-3">Clique em "Novo Fornecedor" para começar</p>
                    <button class="btn btn-danger btn-sm" onclick="openAddModal()">
                        <i class="bi bi-plus-circle me-2"></i>
                        Adicionar Fornecedor
                    </button>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = suppliers.map(fornecedor => {
        const id = fornecedor.id;
        const nome = fornecedor.nome || '—';
        const email = fornecedor.email || '—';
        const telefone = fornecedor.telefone || '—';
        const nomeRepresentante = fornecedor.representante?.nomeRepresentante || '—';
        const ativoBadge = fornecedor.ativo
            ? `<span class="badge bg-success">Ativo</span>`
            : `<span class="badge bg-secondary">Inativo</span>`;

        return `
            <tr>
                <td><span class="badge-id">#${id}</span></td>
                <td class="fw-semibold">${escapeHtml(nome)}</td>
                <td>${escapeHtml(email)}</td>
                <td>${escapeHtml(telefone)}</td>
                <td>${escapeHtml(nomeRepresentante)}</td>
                <td>${ativoBadge}</td>
                <td class="text-end">
                    <button class="btn btn-action btn-action-delete" onclick="openDeleteModal(${id}, '${escapeHtml(nome)}')" title="Desativar fornecedor">
                        <i class="bi bi-slash-circle"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function renderPagination() {
    const pagination = document.getElementById('pagination');
    let paginationHtml = '';

    paginationHtml += `
        <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage - 1}); return false;">
                <i class="bi bi-chevron-left"></i>
            </a>
        </li>
    `;

    for (let i = 1; i <= totalPages; i++) {
        paginationHtml += `
            <li class="page-item ${currentPage === i ? 'active' : ''}">
                <a class="page-link" href="#" onclick="changePage(${i}); return false;">${i}</a>
            </li>
        `;
    }

    paginationHtml += `
        <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage + 1}); return false;">
                <i class="bi bi-chevron-right"></i>
            </a>
        </li>
    `;

    pagination.innerHTML = paginationHtml;
}

function updatePaginationInfo(startIndex, endIndex) {
    const info = document.getElementById('paginationInfo');
    info.textContent = `Mostrando ${startIndex + 1}-${endIndex} de ${allSuppliers.length} fornecedores`;
}

function changePage(page) {
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    renderCurrentPage();
    renderPagination();
}

function showError(message) {
    const tbody = document.getElementById('supplierTable');
    tbody.innerHTML = `
        <tr>
            <td colspan="7" class="text-center py-4 text-danger">
                <i class="bi bi-exclamation-circle fs-3 d-block mb-2"></i>
                ${message}
            </td>
        </tr>
    `;
    document.getElementById('paginationContainer').style.display = 'none';
}

function showSuccess(message) {
    const alertContainer = document.getElementById('alertContainer');
    const alertId = 'alert-' + Date.now();

    alertContainer.innerHTML = `
        <div id="${alertId}" class="alert alert-success alert-dismissible fade show alert-toast" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;

    setTimeout(() => {
        const el = document.getElementById(alertId);
        if (el) el.remove();
    }, 3000);
}

function openAddModal() {
    document.getElementById('modalTitle').textContent = 'Novo Fornecedor';
    document.getElementById('supplierForm').reset();
    document.getElementById('supplierId').value = '';
    supplierModal.show();
}

async function saveSupplier() {
    const nome = document.getElementById('nome').value.trim();
    const email = document.getElementById('email').value.trim();
    const telefone = document.getElementById('telefone').value.trim();
    const nomeRepresentante = document.getElementById('nomeRepresentante').value.trim();
    const telefoneRepresentante = document.getElementById('telefoneRepresentante').value.trim();
    const emailRepresentante = document.getElementById('emailRepresentante').value.trim();

    if (!nome) {
        alert('Por favor, preencha o nome do fornecedor');
        document.getElementById('nome').focus();
        return;
    }

    if (!email) {
        alert('Por favor, preencha o e-mail do fornecedor');
        document.getElementById('email').focus();
        return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        alert('Por favor, informe um e-mail válido');
        document.getElementById('email').focus();
        return;
    }

    if (!telefone) {
        alert('Por favor, preencha o telefone fixo do fornecedor');
        document.getElementById('telefone').focus();
        return;
    }

    if (telefone.replace(/\D/g, '').length !== 10) {
        alert('Por favor, informe um telefone fixo válido com DDD: (XX) XXXX-XXXX');
        document.getElementById('telefone').focus();
        return;
    }

    const supplierData = {
        nome,
        email,
        telefone
    };

    if (nomeRepresentante || telefoneRepresentante || emailRepresentante) {
        supplierData.representante = {
            nomeRepresentante: nomeRepresentante || null,
            telefoneRepresentante: telefoneRepresentante || null,
            emailRepresentante: emailRepresentante || null
        };
    }

    try {
        const response = await fetch(`${API_BASE_URL}/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(supplierData)
        });

        const data = await response.json();

        if (response.ok) {
            supplierModal.hide();
            await loadSuppliers();
            showSuccess('Fornecedor adicionado com sucesso!');
        } else {
            alert('Erro ao salvar fornecedor: ' + (data.message || 'Erro desconhecido'));
        }
    } catch (error) {
        alert('Erro de conexão com o servidor');
    }
}

function openDeleteModal(id, nome) {
    supplierToDelete = id;
    document.getElementById('deleteSupplierName').textContent = nome;
    deleteModal.show();
}

function closeDeleteModal() {
    deleteModal.hide();
    supplierToDelete = null;
}

async function confirmDelete() {
    if (!supplierToDelete) return;

    try {
        const response = await fetch(`${API_BASE_URL}/disable/${supplierToDelete}`, {
            method: 'POST',
            headers: { 'Accept': 'application/json' }
        });

        const data = await response.json();

        if (response.ok) {
            closeDeleteModal();
            await loadSuppliers();
            showSuccess('Fornecedor desativado com sucesso!');
        } else {
            alert('Erro ao desativar fornecedor: ' + (data.message || 'Erro desconhecido'));
        }
    } catch (error) {
        alert('Erro de conexão com o servidor');
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
