package com.example.matchmaking_mate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> users;
    private OnUserClickListener listener;

    private List<User> filteredUsers;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(Context context, List<User> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        List<String> ListGames = user.getFavoriteGames();
        holder.tvName.setText(user.getFullname());

        if (ListGames == null || ListGames.isEmpty()){
            //holder.tvGames.setVisibility(View.GONE);
            holder.tvGames.setText("");
        }

        else{
            //holder.tvGames.setVisibility(View.VISIBLE);
            String games = ListGames.toString().replace("[","").replace("]","").trim();
            holder.tvGames.setText(games);

        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvGames;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemFullName);
            tvGames = itemView.findViewById(R.id.tvItemListGames);
        }
    }

}