package com.minimaldev.android.facerec;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ScanFragment extends Fragment  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    int flag=0;
    private String mParam2;
    Boolean f;

    String new_url = "https://api.kairos.com/enroll";
    String recog_url = "https://api.kairos.com/recognize";
    String la,lo;

    String b64url, imageFileName, path;

    final String SUBS_KEY="2741f804e503419fa4a4cd0ef9c8f142";
    final String ACCESS_KEY="AKIAIBMPSPJMFPLIEONA";
    final String SECRET_KEY="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";
    final String POOL_ID="us-east-1:455d3f28-b54f-479a-8f56-dbdfee905997";
    final String BUCKET_NAME="332609";

    float d1[] = new float[1];

    float d2[] = new float[1];

    int inside=0;
    File image;
    String username;
    SharedPreferences.Editor editor;
    boolean check;

    Boolean in;
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private double latitude, longitude;

    private Location mLastLocation, mCurrentLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;

    View v;
    String date,time,registration;
    Button getData,Onsubmit;

    String sendRegistration,sendDate,sendTime;


    ProgressDialog progressBar;
    LocationManager lm;
    //ProgressDialog dialog;
    String user;

    double latitiude_current, longitude_current, lat_srmUB = 12.823414, long_srmUB = 80.042404, lat_srmMain=12.820622, long_srmMain=80.039574;
    View view;
    SharedPreferences sharedPreferences1;

    Button button;
    Bitmap pic, photo;

    TextView textView;
    //private OnFragmentInteractionListener mListener;

    public ScanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScanFragment newInstance(String param1, String param2) {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Intent intent = getActivity().getIntent();
        //username = intent.getStringExtra("username");


        SharedPreferences name= PreferenceManager.getDefaultSharedPreferences(getActivity());
        username=name.getString("username",null);

        registration=name.getString("registration",null);

        //Toast.makeText(getActivity(),username,Toast.LENGTH_SHORT).show();

        progressBar=new ProgressDialog(getActivity());
       // progressBar.setTitle("Loading");
        progressBar.setMessage("Please wait...");
        progressBar.setCancelable(false);
        progressBar.setCanceledOnTouchOutside(false);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_scan, container, false);
        //button = (Button) view.findViewById(R.id.checkin);

        if(!isNetworkAvailable())
        {
            Snackbar snackbar=Snackbar.make(container, "No internet connection !", Snackbar.LENGTH_LONG);
            View snack=snackbar.getView();
            TextView t=(TextView)snack.findViewById(android.support.design.R.id.snackbar_text);
            t.setTextColor(Color.parseColor("#ffffff"));
            snackbar.show();
        }

        SharedPreferences sr= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String imgurl="";
        imgurl=sr.getString("img_url",null);

        byte[] decode=null;

        decode= Base64.decode(imgurl, Base64.DEFAULT);
        Bitmap b = BitmapFactory.decodeByteArray(decode, 0, decode.length);


        ImageView imageView = (ImageView) view.findViewById(R.id.profile);
        imageView.setImageBitmap(b);


        in = ((MainActivity) getActivity()).getLocation();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab2);

        final Button btn=(Button)view.findViewById(R.id.checkin);
        lm=(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        final FloatingActionButton refreshbtn = (FloatingActionButton) view.findViewById(R.id.refreshbutton);

        Button textView=(Button) view.findViewById(R.id.logoutscan);

        SharedPreferences sharedp=PreferenceManager.getDefaultSharedPreferences(getActivity());
        String name=sharedp.getString("name","Hello");

        TextView n=(TextView)view.findViewById(R.id.username);
        n.setText(name);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putInt("logout",1);
                        editor.apply();

                        SharedPreferences p= PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor e=p.edit();
                        e.putInt("checkedout",0);
                        e.apply();

                        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor edtr=preferences.edit();
                        edtr.putInt("checkedin",0);
                        edtr.apply();

                        SharedPreferences na= PreferenceManager.getDefaultSharedPreferences(getActivity());
                        final SharedPreferences.Editor edi=na.edit();
                        edi.putInt("login",0);
                        edi.apply();

                        Toast.makeText(getActivity(),"Successfully logged out",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(getActivity(), LaunchActivity.class);
                        startActivity(intent);
                        getActivity().finish();

                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setTitle("Log out");
                builder.setMessage("Are you sure you want to log out?");
                AlertDialog alertDialog=builder.create();
                alertDialog.show();


            }
        });

        lm=(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        final boolean en=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!en) {
            //fab.setEnabled(false);
            //btn.setEnabled(false);
            Snackbar snackbar=Snackbar.make(container, "Location services aren't enabled !", Snackbar.LENGTH_LONG);
            View snack=snackbar.getView();
            TextView t=(TextView)snack.findViewById(android.support.design.R.id.snackbar_text);
            t.setTextColor(Color.parseColor("#ffffff"));

            snackbar.show();

        }
        else {



            if (!in) {

                //inside = 1;
                final Snackbar snackbar = Snackbar.make(container, "You're not inside the campus !", Snackbar.LENGTH_LONG);
                View snack = snackbar.getView();
                TextView t = (TextView) snack.findViewById(android.support.design.R.id.snackbar_text);
                t.setTextColor(Color.parseColor("#ffffff"));

                snackbar.setAction("Refresh", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //snackbar.dismiss();
                        ((MainActivity) getActivity()).getLocation();

                        //Toast.makeText(getActivity(),"Refreshing...",Toast.LENGTH_SHORT).show();

                        Fragment fr=getActivity().getSupportFragmentManager().findFragmentById(R.id.linear_replace);
                        if(fr instanceof ScanFragment){
                            FragmentTransaction fragmentTransaction=(getActivity()).getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.detach(fr);
                            fragmentTransaction.attach(fr);
                            fragmentTransaction.commit();
                        }

                    }
                });

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        snackbar.setActionTextColor(Color.parseColor("#1f7ae1"));
                        snackbar.show();
                    }

                });

                snackbar.setActionTextColor(Color.parseColor("#1f7ae1"));
                snackbar.show();
            } else {
                //inside = 0;

                Snackbar snackbar = Snackbar.make(container, "Welcome to SRM University !", Snackbar.LENGTH_SHORT);
                View snack = snackbar.getView();
                TextView t = (TextView) snack.findViewById(android.support.design.R.id.snackbar_text);
                t.setTextColor(Color.parseColor("#ffffff"));

                snackbar.show();

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dispatchTakePictureIntent();


                    }

                });

            }
        }
        if(en && in) {

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    dispatchTakePictureIntent();

                }

            });
        }


        refreshbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Snackbar snackbar=Snackbar.make(container, "Refreshing...", Snackbar.LENGTH_LONG);
                View snack=snackbar.getView();
                TextView t=(TextView)snack.findViewById(android.support.design.R.id.snackbar_text);
                t.setTextColor(Color.parseColor("#ffffff"));
                snackbar.show();

                ((MainActivity) getActivity()).getLocation();

                //Toast.makeText(getActivity(),"Refreshing...",Toast.LENGTH_SHORT).show();

               Fragment fr=getActivity().getSupportFragmentManager().findFragmentById(R.id.linear_replace);
                if(fr instanceof ScanFragment){
                    FragmentTransaction fragmentTransaction=(getActivity()).getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.detach(fr);
                    fragmentTransaction.attach(fr);
                    fragmentTransaction.commit();
                }
            }
        });


        //**** Check-in Button and Camera button in one ****


        return view;
    }


    @Override
    public void onResume()
    {
        super.onResume();

    }

    public boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager=(ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnected();
    }


    public void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),"com.minimaldev.android.fileprovider",photoFile );

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            /*pic = (Bitmap) data.getExtras().get("data");
            photo = pic;


            ImageView im = (ImageView) view.findViewById(R.id.image2);
            im.setImageBitmap(pic);
            im.buildDrawingCache();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //Bitmap bitmap = im.getDrawingCache();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imagebytes = byteArrayOutputStream.toByteArray();
            b64url = Base64.encodeToString(imagebytes, Base64.DEFAULT);

            verify(); */
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
                Toast.makeText(getActivity(),"Error",Toast.LENGTH_LONG).show();
            }


            BasicAWSCredentials credentials=new BasicAWSCredentials(ACCESS_KEY,SECRET_KEY);

            CognitoCachingCredentialsProvider credentialsProvider;
            credentialsProvider=new CognitoCachingCredentialsProvider(getActivity(),POOL_ID, Regions.US_EAST_1);

            AmazonS3Client s3Client=new AmazonS3Client(credentialsProvider);
            s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));


            //byte[] bytes=new byte[(int)f.length()];

            final AmazonRekognitionClient amazonRekognitionClient=new AmazonRekognitionClient(credentialsProvider);

            //InputStream fin1=getResources().openRawResource(R.raw.dsc);
            InputStream fin2= null;
            try {
                fin2 = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            final Image image=new Image();
            final Image image1=new Image();
            try {
                InputStream inputStream= new FileInputStream(f.getAbsoluteFile().toString());
                ByteBuffer byteBuffer=ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
                Log.d("MESSAGE: ", Arrays.toString(byteBuffer.array()));

                image.withBytes(ByteBuffer.wrap(IOUtils.toByteArray(fin2)));
                System.out.println( "REG no. " + username );
                image1.withS3Object(new S3Object().withName(username+".jpg").withBucket(BUCKET_NAME));

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        CompareFacesRequest request = new CompareFacesRequest()
                                .withSourceImage(image)
                                .withTargetImage(image1).withSimilarityThreshold(90f);
                        CompareFacesResult response = amazonRekognitionClient.compareFaces(request);

                        response.getFaceMatches();
                        //System.out.println("RESPONSE-------> "+response.toString());

                        List<CompareFacesMatch> faceDetails = response.getFaceMatches();
                        for (CompareFacesMatch match: faceDetails)
                        {
                            ComparedFace face= match.getFace();
                            BoundingBox position = face.getBoundingBox();
                            System.out.println("Face at " + position.getLeft().toString()
                                    + " " + position.getTop()
                                    + " matches with " + face.getConfidence().toString()
                                    + "% confidence.");

                            int conf=face.getConfidence().intValue();
                            System.out.println("C O N F I D E N C E --->"+conf);
                            if(conf>=90)
                            {
                                flag=0;
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {



                                        try {

                                            //sharedPreferences1= getActivity().getSharedPreferences("checkprefs", 0);

                                            //Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_LONG).show();

                                            Snackbar snackbar=Snackbar.make(getActivity().findViewById(R.id.linear_main), "Successful. Checking you in...", Snackbar.LENGTH_LONG);
                                            View snack=snackbar.getView();
                                            TextView t=(TextView)snack.findViewById(android.support.design.R.id.snackbar_text);
                                            t.setTextColor(Color.parseColor("#ffffff"));

                                            snackbar.show();

                                            //Toast.makeText(getActivity(),"Please wait...checking you in !",Toast.LENGTH_LONG).show();
                                            String getDate =  new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                                            System.out.println(getDate);
                                            String getTime = new SimpleDateFormat("HH:mm:ss").format(new Date());


                                            sendRegistration = username;
                                            //Toast.makeText(getActivity(),sendRegistration,Toast.LENGTH_SHORT).show();
                                            sendDate = getDate;
                                            //Toast.makeText(getActivity(),sendDate,Toast.LENGTH_SHORT).show();
                                            sendTime = getTime;
                                            //Toast.makeText(getActivity(),sendTime,Toast.LENGTH_SHORT).show();
                                            String url="http://attendancecom.000webhostapp.com/connect/register.php";


                                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    //Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
                                                    if(response.equals("ATTENDANCE MARKED!!"))
                                                    {
                                                        //System.out.println(response);


                                                        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getActivity());
                                                        SharedPreferences.Editor editor=sharedPreferences.edit();
                                                        editor.putInt("checkedin",1);
                                                        editor.apply();

                                                        SharedPreferences na= PreferenceManager.getDefaultSharedPreferences(getActivity());
                                                        final SharedPreferences.Editor edi=na.edit();
                                                        edi.putInt("login",1);
                                                        edi.apply();

                                                        Intent intent=new Intent(getActivity(),LogOutActivity.class);
                                                        intent.putExtra("username",username);
                                                        startActivity(intent);
                                                        getActivity().finish();

                                                    }

                                                }

                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {

                                                    if(error!=null && error.getMessage()!=null)
                                                        Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(getActivity(),"Hello",Toast.LENGTH_SHORT).show();


                                                }
                                            }) {
                                                @Override
                                                protected Map<String, String> getParams() {
                                                    Map<String, String> params = new HashMap<String, String>();
                                                    params.put("registration", username);
                                                    params.put("date", sendDate);
                                                    params.put("time", sendTime);
                                                    return params;
                                                }

                                                @Override
                                                public Map<String, String> getHeaders() throws AuthFailureError {
                                                    Map<String, String> Headers = new HashMap<String, String>();
                                                    Headers.put("User-agent", "MiniProject");
                                                    return Headers;
                                                }
                                            };

                                            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                            MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);


                                            //sharedPreferences1.edit().putBoolean("check",true).apply();
                                            //ScanFragment.this.check=true;



                                            // editor.apply();
                                        } catch (Exception e) {
                                            Log.e("Error :", e.getMessage());
                                            // Toast.makeText(getActivity(), "No faces found !",Toast.LENGTH_LONG).show();
                                        }

                                    }
                                });
                            }
                            else
                            {

                                flag=1;
                               // Toast.makeText(getActivity(), "Unsuccessful", Toast.LENGTH_LONG).show();


                            }

                        }
                        List<ComparedFace> uncompared = response.getUnmatchedFaces();

                        System.out.println("There were " + uncompared.size()
                                + " that did not match");
                        if(uncompared.size()>0)
                        {
                            flag=1;
                        }
                        System.out.println("Source image rotation: " + response.getSourceImageOrientationCorrection());
                        System.out.println("target image rotation: " + response.getTargetImageOrientationCorrection());

                        // Toast.makeText(UploadFaceSignUp.this,"Success",Toast.LENGTH_LONG).show();
                    }
                });


                if(flag==1)
                    Toast.makeText(getActivity(),"Unsuccessful",Toast.LENGTH_LONG).show();
                //FileInputStream fileInputStream=new FileInputStream(f);
                //fileInputStream.read(bytes);
                //fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }


            //            E N D    H E R E    ! ! ! !



        } else {

             Toast.makeText(getActivity(),"Error! Try again.",Toast.LENGTH_LONG).show();
        }


    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        imageFileName = "JPEG_" + timeStamp + "_" + username;
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        path = image.getAbsolutePath();
        System.out.println(path);

        return image;
    }


    private class Verifytask extends AsyncTask<String, Void, String> {

        MainActivity mainActivity;
        String url;

        String status;

        public Verifytask() {

            // dialog=new ProgressDialog(Main);
        }

        @Override
        protected  void onPreExecute()
        {
            progressBar.show();
        }


        @Override
        protected String doInBackground(String... params) {


            return "yes";
        }

        @Override
        public void onPostExecute(String res) {
           // Toast.makeText(getActivity(), res, Toast.LENGTH_LONG).show();
            progressBar.dismiss();


        }
    }



}
