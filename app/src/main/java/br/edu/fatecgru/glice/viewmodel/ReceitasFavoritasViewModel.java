package br.edu.fatecgru.glice.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import br.edu.fatecgru.glice.model.Receita;
import br.edu.fatecgru.glice.repository.ReceitaFavoritaRepository;

/**
 * ViewModel responsável por fornecer e gerenciar a lista de Receitas Favoritas
 * obtidas do Firestore (via ReceitaFavoritaRepository) para o Fragment.
 *
 * NOTA: Este ViewModel foi corrigido para usar o LiveData reativo diretamente
 * do Repositório, eliminando a necessidade de callbacks manuais (como onFavoritasLoaded/onError).
 */
public class ReceitasFavoritasViewModel extends ViewModel {

    private final ReceitaFavoritaRepository repository;
    // O LiveData agora apenas repassa o LiveData do Repositório
    public final LiveData<List<Receita>> favoritas;

    // Construtor: Inicializa o repositório e o LiveData
    public ReceitasFavoritasViewModel() {
        // O repositório inicia o listener do Firestore em seu construtor
        this.repository = new ReceitaFavoritaRepository();
        // A CORREÇÃO PRINCIPAL: Obter o LiveData diretamente do repositório.
        // O método loadFavoritas() e os callbacks não são mais necessários.
        this.favoritas = repository.getReceitasFavoritas();
    }

    /**
     * Lógica para remover uma receita da lista de favoritos no Firestore.
     * Chama o método delete do repositório, que é mais idiomático.
     */
    public void removerFavorita(Receita receita) {
        // Assume que o objeto Receita contém o DocumentId necessário para exclusão
        // Chamamos 'delete' no repositório, que espera o objeto Receita completo.
        repository.delete(receita);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Garante que o listener do Firestore seja removido quando o ViewModel for destruído
        repository.removerListener();
    }
}