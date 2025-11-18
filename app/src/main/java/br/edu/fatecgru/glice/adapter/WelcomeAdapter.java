package br.edu.fatecgru.glice.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import br.edu.fatecgru.glice.R;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class CarrosselAdapter extends RecyclerView.Adapter<CarrosselAdapter.CarrosselViewHolder> {

    private List<CarrosselItem> itens;

    public CarrosselAdapter(List<CarrosselItem> itens) {
        this.itens = itens;
    }

    @NonNull
    @Override
    public CarrosselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carrossel_welcome, parent, false);
        return new CarrosselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarrosselViewHolder holder, int position) {
        CarrosselItem item = itens.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class CarrosselViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView boasVindasImage;
        private TextView tvTitle;
        private TextView tvDescription;

        public CarrosselViewHolder(@NonNull View itemView) {
            super(itemView);
            boasVindasImage = itemView.findViewById(R.id.boas_vindas_image);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        public void bind(CarrosselItem item) {
            boasVindasImage.setImageResource(item.getImageRes());
            tvTitle.setText(item.getTitle());
            tvDescription.setText(item.getDescription());
        }
    }
}