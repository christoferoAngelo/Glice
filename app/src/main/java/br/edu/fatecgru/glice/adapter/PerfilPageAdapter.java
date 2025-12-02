package br.edu.fatecgru.glice.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import br.edu.fatecgru.glice.fragment.LivroReceitasFragment;
import br.edu.fatecgru.glice.fragment.ReceitasFavoritasFragment;

/**
 * Adapter para o ViewPager2 da PerfilActivity.
 * Gerencia a navegação entre LivroReceitasFragment (local) e ReceitasFavoritasFragment (API/Firestore).
 */
public class PerfilPageAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 2;

    public PerfilPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Retorna o Fragment apropriado para cada aba
        switch (position) {
            case 0:
                return new LivroReceitasFragment(); // Aba 1: Receitas Locais (Room)
            case 1:
                return new ReceitasFavoritasFragment(); // Aba 2: Receitas Favoritas (Firestore)
            default:
                // Devemos sempre retornar um fragment em caso de erro, ou a aplicação pode falhar
                return new LivroReceitasFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}