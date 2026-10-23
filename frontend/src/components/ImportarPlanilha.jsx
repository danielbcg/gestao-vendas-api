import React, { useState } from 'react';
import ModalEditarVenda from './ModalEditarVenda';

export default function ImportarPlanilha({ iaConfigurada, produtos, onVendasAtualizadas }) {
  const [arquivo, setArquivo] = useState(null);
  const [importando, setImportando] = useState(false);
  const [relatorio, setRelatorio] = useState(null);
  const [erro, setErro] = useState(null);
  const [vendaEmEdicao, setVendaEmEdicao] = useState(null);

  const handleArquivoChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setArquivo(e.target.files[0]);
      setErro(null);
    }
  };

  const handleImportar = async (e) => {
    e.preventDefault();
    if (!arquivo) {
      setErro('Por favor, selecione um arquivo Excel (.xlsx) antes de continuar.');
      return;
    }

    setImportando(true);
    setErro(null);
    setRelatorio(null);

    const formData = new FormData();
    formData.append('arquivo', arquivo);

    try {
      const res = await fetch('http://localhost:8080/api/importacao', {
        method: 'POST',
        body: formData
      });

      if (!res.ok) {
        const errJson = await res.json();
        setErro(errJson.mensagem || 'Ocorreu um erro ao importar a planilha.');
        setImportando(false);
        return;
      }

      const data = await res.json();
      setRelatorio(data);
      onVendasAtualizadas();
    } catch (err) {
      setErro('Não foi possível conectar ao servidor. Certifique-se de que o backend está em execução.');
    } finally {
      setImportando(false);
    }
  };

  const handleAbrirEdicao = async (vendaId) => {
    try {
      const res = await fetch(`http://localhost:8080/api/vendas/${vendaId}`);
      if (res.ok) {
        const v = await res.json();
        setVendaEmEdicao(v);
      } else {
        alert('Não foi possível carregar os dados da venda selecionada.');
      }
    } catch (err) {
      alert('Erro de conexão ao buscar os detalhes da venda.');
    }
  };

  const handleVendaSalva = (vendaAtualizada) => {
    if (relatorio) {
      // Remove da lista de pendentes de revisão do relatório atual
      const atualizados = relatorio.importadasComRevisao.filter(
        item => item.vendaId !== vendaAtualizada.id
      );
      setRelatorio({
        ...relatorio,
        importadasComRevisao: atualizados,
        importadasAutomaticamente: relatorio.importadasAutomaticamente + 1
      });
    }
    onVendasAtualizadas();
  };

  return (
    <div>
      <div className="card">
        <h2 className="card-title">📥 Importar Planilha de Vendas</h2>
        <p className="brand-subtitle" style={{ marginBottom: 20 }}>
          Envie sua planilha Excel manual (como a <code>vendas_maria.xlsx</code>). O sistema vai padronizar os dados automaticamente e apontar o que precisa de conferência.
        </p>

        {erro && <div className="alert alert-error">{erro}</div>}

        <form onSubmit={handleImportar} style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
          <input
            type="file"
            accept=".xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            onChange={handleArquivoChange}
            className="form-input"
            style={{ maxWidth: 400 }}
          />
          <button type="submit" className="btn btn-primary" disabled={importando}>
            {importando ? (
              <>
                <span className="spinner"></span>
                <span>Padronizando dados...</span>
              </>
            ) : (
              'Importar planilha'
            )}
          </button>
        </form>
      </div>

      {relatorio && (
        <div>
          {/* Cards de Resumo */}
          <div className="totais-grid">
            <div className="total-card">
              <div className="total-label">Vendas Importadas</div>
              <div className="total-value vendas">
                {relatorio.importadasAutomaticamente}
              </div>
              <p className="field-hint" style={{ marginTop: 4 }}>Padronizadas com sucesso</p>
            </div>

            <div className="total-card">
              <div className="total-label">Precisam de Conferência</div>
              <div className="total-value devolucoes">
                {relatorio.importadasComRevisao.length}
              </div>
              <p className="field-hint" style={{ marginTop: 4 }}>Aguardando seu toque</p>
            </div>

            <div className="total-card">
              <div className="total-label">Linhas Ignoradas</div>
              <div className="total-value liquido">
                {relatorio.ignoradas.length}
              </div>
              <p className="field-hint" style={{ marginTop: 4 }}>Duplicadas ou vazias</p>
            </div>
          </div>

          {/* Lista de Revisões Pendentes */}
          {relatorio.importadasComRevisao.length > 0 && (
            <div className="card">
              <h3 className="card-title">⚠️ Linhas que precisam de conferência</h3>
              <p className="brand-subtitle" style={{ marginBottom: 16 }}>
                Essas linhas foram importadas com valores padrão provisórios para você não perder nenhuma venda. Clique em "Conferir" para ajustar ou pedir uma sugestão à Inteligência Artificial.
              </p>

              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Linha</th>
                      <th>Campo</th>
                      <th>Na Planilha Estava</th>
                      <th>O que aconteceu</th>
                      <th>Ação</th>
                    </tr>
                  </thead>
                  <tbody>
                    {relatorio.importadasComRevisao.map((item, idx) => (
                      <tr key={idx} className="row-revisao">
                        <td><strong>#{item.linha}</strong></td>
                        <td>
                          <span className="badge badge-warning">{item.campo}</span>
                        </td>
                        <td>{item.valorOriginal || <em>(em branco)</em>}</td>
                        <td>{item.motivo}</td>
                        <td>
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() => handleAbrirEdicao(item.vendaId)}
                          >
                            {iaConfigurada ? 'Conferir / Sugestão ✨' : 'Conferir'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Linhas Ignoradas */}
          {relatorio.ignoradas.length > 0 && (
            <div className="card">
              <h3 className="card-title">ℹ️ Linhas ignoradas</h3>
              <div className="table-responsive">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Linha da Planilha</th>
                      <th>Motivo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {relatorio.ignoradas.map((item, idx) => (
                      <tr key={idx}>
                        <td><strong>#{item.linha}</strong></td>
                        <td>{item.motivo}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {vendaEmEdicao && (
        <ModalEditarVenda
          venda={vendaEmEdicao}
          produtos={produtos}
          iaConfigurada={iaConfigurada}
          onClose={() => setVendaEmEdicao(null)}
          onSalvo={handleVendaSalva}
        />
      )}
    </div>
  );
}

