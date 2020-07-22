package com.abhish.blogapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.abhish.blogapp.Upload;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VisionImagesAdapter extends FirebaseRecyclerAdapter<Upload,VisionImagesAdapter.VisionImagesViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    private View mainContext;
    private DatabaseReference dbRef;

    public VisionImagesAdapter(@NonNull FirebaseRecyclerOptions<Upload> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final VisionImagesAdapter.VisionImagesViewHolder holder, int position, @NonNull final Upload model) {

        holder.load_anim.setVisibility(View.VISIBLE);
        Glide.with(mainContext).load(model.getImageUrl()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                holder.load_anim.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.load_anim.setVisibility(View.GONE);

                return false;
            }
        }).into(holder.imageView);


        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                CharSequence options[]=new CharSequence[]{
                        "Remove"
                };

                AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Edit change :");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0)
                        {
                            dbRef.child(model.getVisionID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(mainContext.getContext(), "Removed succesfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                builder.show();

                return true;
            }
        });


        holder.visionTitle.setText(model.getVisionName());


    }

    @NonNull
    @Override
    public VisionImagesAdapter.VisionImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vision_image_layout, parent, false);
        return new VisionImagesAdapter.VisionImagesViewHolder(view);
    }


    public class VisionImagesViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView,pStatus,cStatus;
        ImageView load_anim;
        TextView visionTitle,visionDesc;


        public VisionImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            mainContext=itemView;
            dbRef= FirebaseDatabase.getInstance().getReference().child("Blogs").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            imageView=itemView.findViewById(R.id.imageview);
            load_anim=itemView.findViewById(R.id.load_anim);
            visionTitle=itemView.findViewById(R.id.vision_title);
            visionDesc=itemView.findViewById(R.id.vision_desc);
        }
    }

}
