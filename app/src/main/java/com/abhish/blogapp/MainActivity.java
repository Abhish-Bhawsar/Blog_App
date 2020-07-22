package com.abhish.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private VisionImagesAdapter visionImagesAdapter;
    private RecyclerView recyclerView;
    String deviceId;
    private static final int PICK_IMAGE_REQUEST=1;
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton fabNewVision;
    private Uri imageUri;
    SpinKitView spinKitView;
    LinearLayout noTask;
    EditText visionTitle,visionDesc;
    ImageView visionImage;
    Button vision_save;
    View mview;
    SpinKitView progressBar;

    AlertDialog alertDialog;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    int i=1;

    private FirebaseAuth mAuth;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mToolbar=findViewById(R.id.mtool_bar);
        mAuth=FirebaseAuth.getInstance();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Blog App");

        floatingActionMenu=findViewById(R.id.menu_labels_right);
        fabNewVision=findViewById(R.id.fab_add_vision);
        recyclerView=findViewById(R.id.recycler_list);
        spinKitView=findViewById(R.id.spin_kit);
        noTask=findViewById(R.id.no_task);
        mStorageRef= FirebaseStorage.getInstance().getReference("Blogs/"+mAuth.getCurrentUser().getUid());
        mDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Blogs").child(mAuth.getCurrentUser().getUid());

        fabNewVision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });


        recyclerView=findViewById(R.id.recycler_list);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<Upload> options =
                new FirebaseRecyclerOptions.Builder<Upload>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Blogs").child(mAuth.getCurrentUser().getUid())
                                , Upload.class).build();

        visionImagesAdapter=new VisionImagesAdapter(options);
        recyclerView.setAdapter(visionImagesAdapter);
        visionImagesAdapter.startListening();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid()))
                {
                    noTask.setVisibility(View.GONE);
                }
                else
                {
                    spinKitView.setVisibility(View.GONE);
                    noTask.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
        }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null)
        {
            startActivity(new Intent(MainActivity.this,LoginOptionActivity.class));
        }
        else
        {
            Toast.makeText(this, "Successfull", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.main_logout_option)
        {
            mAuth.signOut();
            sendUserToLoginOptionActivity();
        }
        return true;
    }

    private void sendUserToLoginOptionActivity() {
        Intent loginIntent=new Intent(MainActivity.this,LoginOptionActivity.class);
        startActivity(loginIntent);
    }

    private void openDialog() {
        final AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(MainActivity.this);
        mview= getLayoutInflater().inflate(R.layout.layout_add_vision,null);
        visionTitle=mview.findViewById(R.id.vision_title);
        visionDesc=mview.findViewById(R.id.vision_desc);
        visionImage=mview.findViewById(R.id.vision_image);
        vision_save=mview.findViewById(R.id.save_vision);
        progressBar=mview.findViewById(R.id.progress_barr);

        Button cancelb=mview.findViewById(R.id.cancel_vision);
        visionTitle.setText("");
        visionDesc.setText("");
        imageUri=null;

        visionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        vision_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadVision();
            }
        });

        cancelb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                floatingActionMenu.close(true);
            }
        });

        alertDialogBuilder.setView(mview);
        alertDialog=alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver=this.getContentResolver();
        MimeTypeMap mimeTypeMap =MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadVision() {
        if (imageUri !=null && !TextUtils.isEmpty(visionTitle.getText()))
        {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(0);
                        }
                    },5000);
                    Toast.makeText(MainActivity.this, "Upload Successfull", Toast.LENGTH_SHORT).show();

                    //uploadItems=new ArrayList<>();

                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String uploadId=mDatabaseRef.push().getKey();
                                    Upload upload=new Upload(uploadId,visionTitle.getText().toString().trim(),uri.toString(),visionDesc.getText().toString());
                                    mDatabaseRef.child(uploadId).setValue(upload);
                                }
                            });
                        }
                    }
                    alertDialog.dismiss();
                    floatingActionMenu.close(true);
                    progressBar.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    progressBar.setVisibility(View.GONE);

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress =(100 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }
        else
        {
            Toast.makeText(this, "Enter vision title, select image, taskStatus, Date should be there", Toast.LENGTH_SHORT).show();
        }

    }

    private void openFileChooser() {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data !=null && data.getData()!=null)
        {
            imageUri=data.getData();
            Picasso.get().load(imageUri).into(visionImage);
        }
    }

}
