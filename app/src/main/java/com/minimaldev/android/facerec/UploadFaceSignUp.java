package com.minimaldev.android.facerec;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ANUJ on 11/09/2017.
 */

public class UploadFaceSignUp extends AppCompatActivity
{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
     int f=0;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    byte[] imagebytes;
    final  String key="My_prefs";

    String new_url="https://centralindia.api.cognitive.microsoft.com/face/v1.0/detect?returnFaceId=true";
    String saveUrl="";

    //Boolean f;
    String path,imageFileName,originalb64;
    //Context context;

    //View view;
    //private  static String key="My Pref";

    //NavigationView navigationView;

   // private static String countstring="count";

    SharedPreferences sharedPreferences,sharedPreferences2;

    Bitmap b,pic,photo;
    File image,file;

    int flag=0,count=0,getCounter=0;
    String registration;
    //int inside;
    ProgressDialog progressBar;

    final String SUBS_KEY="2741f804e503419fa4a4cd0ef9c8f142";
    final String ACCESS_KEY="AKIAIBMPSPJMFPLIEONA";
    final String SECRET_KEY="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";
    final String POOL_ID="us-east-1:455d3f28-b54f-479a-8f56-dbdfee905997";
    final String BUCKET_NAME="332609";

    String myid="MY_PHOTO_ID";

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=getIntent();
        registration=intent.getStringExtra("regno");

       // System.out.println(registration);
        setContentView(R.layout.uploadface);

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

        progressBar=new ProgressDialog(this);
        // progressBar.setTitle("Loading");
        progressBar.setMessage("Uploading Image...");
        progressBar.setCancelable(false);
        progressBar.setCanceledOnTouchOutside(false);

        FloatingActionButton floatingActionButton=(FloatingActionButton)findViewById(R.id.floatingbu);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dispatchTakePictureIntent();
               /* try {


                    /*SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(UploadFaceSignUp.this);
                    getCounter = sharedPreferences1.getInt("count", 0);


                    if (getCounter == 1) {
                        Toast.makeText(UploadFaceSignUp.this, "You can upload image only once !", Toast.LENGTH_LONG).show();

                    } else {
                        //flag=0;
                        dispatchTakePictureIntent();

                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                    //Toast.makeText(MainActivity.this, " Upload your image first !",Toast.LENGTH_LONG).show();
                    //flag=0;
                    dispatchTakePictureIntent();

                } */

            }
        });

    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();


            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(UploadFaceSignUp.this,"com.minimaldev.android.fileprovider",photoFile );

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK)
        {

            File f=new File(this.path);
            int size=(int)f.length();

            byte[] b=new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(f));
                buf.read(b,0,b.length);
                buf.close();
            }
            catch(Exception e)
            {
                Toast.makeText(UploadFaceSignUp.this,"Error",Toast.LENGTH_LONG).show();
            }


            //BasicAWSCredentials credentials=new BasicAWSCredentials(ACCESS_KEY,SECRET_KEY);

            CognitoCachingCredentialsProvider credentialsProvider;
            credentialsProvider=new CognitoCachingCredentialsProvider(getApplicationContext(),POOL_ID,Regions.US_EAST_1);

            AmazonS3Client s3Client=new AmazonS3Client(credentialsProvider);
            s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));

            //For uploading !!!

            TransferUtility transferUtility =
                    TransferUtility.builder()
                            .context(getApplicationContext())
                            .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                            .s3Client(s3Client)
                            .build();

            TransferObserver uploadObserver =
                    transferUtility.upload(BUCKET_NAME,
                            registration+".jpg", f);

            // Attach a listener to the observer to get state update and progress notifications
            uploadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        // Handle a completed upload.



                        Toast.makeText(UploadFaceSignUp.this,"Successful",Toast.LENGTH_LONG).show();



                       AsyncTask.execute(new Runnable() {
                           @Override
                           public void run() {


                               String u="http://attendancecom.000webhostapp.com/connect/url.php";
                               StringRequest stringRequest = new StringRequest(Request.Method.POST, u , new Response.Listener<String>() {
                                   @Override
                                   public void onResponse(String response)
                                   {
                                       // System.out.println(response);
                                       // Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
                                       if(response.equals("IMAGE UPLOADED"))
                                       {

                                           progressBar.dismiss();
                                           Toast.makeText(UploadFaceSignUp.this,"Registration Successful !",Toast.LENGTH_LONG).show();


                                           Intent intent1=new Intent(UploadFaceSignUp.this,LoginActivity.class);
                                           startActivity(intent1);
                                           finish();


                                       }
                                       else {
                                           Toast.makeText(UploadFaceSignUp.this, "Error! Try again.", Toast.LENGTH_LONG).show();

                                       }
                                   }

                               }, new Response.ErrorListener() {
                                   @Override
                                   public void onErrorResponse(VolleyError error) {

                                       Toast.makeText(UploadFaceSignUp.this, "Error! Try again.", Toast.LENGTH_LONG).show();
                                       System.out.println(error.toString());

                                   }
                               }) {
                                   @Override
                                   protected Map<String, String> getParams()
                                   {
                                       Map<String, String> params = new HashMap<String, String>();
                                       //System.out.println(registration);
                                       System.out.println(originalb64);
                                       originalb64="trdetre45353";
                                       SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(UploadFaceSignUp.this);
                                       SharedPreferences.Editor editor=sharedPreferences.edit();
                                       editor.putString("registration",registration);
                                       editor.apply();
                                       params.put("img_url", originalb64);
                                       params.put("registration",registration);
                                       return params;
                                   }

                                   @Override
                                   public Map<String, String> getHeaders() throws AuthFailureError {
                                       Map<String, String> Headers = new HashMap<String, String>();
                                       Headers.put("User-agent", "MiniProject");
                                       return Headers;
                                   }
                               };
                               MySingleton.getInstance(UploadFaceSignUp.this).addToRequestQueue(stringRequest);




                           }
                       });


                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                    int percentDone = (int)percentDonef;

                    Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                            + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    // Handle errors
                    Toast.makeText(UploadFaceSignUp.this,"Unsuccessful",Toast.LENGTH_LONG).show();
                }

            });



            //***********************************E N D     H E R E*************************************










            /*Bitmap bitmap= BitmapFactory.decodeFile(path);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            bytes=byteArrayOutputStream.toByteArray(); */



            //ByteBuffer byteBuffer=ByteBuffer.wrap(bytes);




            //Bitmap bitmap = imagev.getDrawingCache();
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            //imagebytes = byteArrayOutputStream.toByteArray();
           // originalb64 = Base64.encodeToString(bytes, Base64.DEFAULT);


            //pic = (Bitmap) data.getExtras().get("data");
                //photo = pic;

                /*ImageView imagev = (ImageView) findViewById(R.id.photoupload);
                imagev.setImageBitmap(pic);
                imagev.buildDrawingCache();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Bitmap bitmap = imagev.getDrawingCache();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                imagebytes = byteArrayOutputStream.toByteArray();
                originalb64 = Base64.encodeToString(imagebytes, Base64.DEFAULT);*/


                //String b64 = originalb64.substring(originalb64.indexOf(",") + 1);

                //sharedPreferences2 = UploadFaceSignUp.this.getSharedPreferences(myid, MODE_PRIVATE);
                //sharedPreferences2.edit().putString("saveb64", b64).apply();


                //Toast.makeText(getActivity(), originalb64, Toast.LENGTH_LONG).show();
                //send();

                //count++;

            /*sharedPreferences = PreferenceManager.getDefaultSharedPreferences(UploadFaceSignUp.this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("count", count);
            editor.apply();
            */

        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_" + Math.random();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        this.path = image.getAbsolutePath();

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(UploadFaceSignUp.this);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("path",path);
        editor.apply();

        // String photoPath = Environment.getExternalStorageDirectory()+"/"+imageFileName;

        //Toast.makeText(getActivity(),path,Toast.LENGTH_LONG).show();

        return image;
    }


    class Asynctsk extends AsyncTask<String ,Void,String> {

        UploadFaceSignUp mainActivity;
        String url;


        public Asynctsk(UploadFaceSignUp Main, String url) {

            this.mainActivity = Main;
            url=this.url;
            // dialog=new ProgressDialog(Main);
        }

        @Override
        protected void onPreExecute(){

            progressBar.show();

        }

        @Override
        protected String doInBackground(String... params) {



            return "yes";
        }

        @Override
        public   void onPostExecute(String res)
        {


            try {



                    // Toast.makeText(getActivity(),res, Toast.LENGTH_LONG).show();

                    //**********UPDATE IMG_URL COLUMN IN DATABASE*********


                    //**************************************




            } catch (Exception e) {
                e.printStackTrace();

            }


            // getActivity().finish();
        }
    }

}
