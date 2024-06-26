package com.example.pichainventory.ui_fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pichainventory.CategoryAdapter;
import com.example.pichainventory.R;
import com.example.pichainventory.Models.Sale;
import com.example.pichainventory.databinding.FragmentStcsaleBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class StcSelFragment extends Fragment {
FragmentStcsaleBinding binding;
    private StorageTask mUploadTask;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    public String ItemName;
    public String ImageUrl;
    public int BuyingPriceValue;
    public String Category;
    public String uploadKey;
    public String uid;
public int dbUnits;
    public int newUnits;
    public StcSelFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStcsaleBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        Bundle args = getArguments();
        if (args != null) {
            String itemName = args.getString("itemName");
            String buyingPrice = args.getString("buyingPrice");
            String imageUrl = args.getString("imageUrl");
            String additional = args.getString("additional");
            String units = args.getString("units");
            String finalSp=args.getString("sellingPrice");
            String category = args.getString("category");
            String bUploadKey= args.getString("mKey");
            String bUid=args.getString("mUid");

            binding.itemLabel.setText(itemName);
            binding.UnitEditText.setText(units);
            binding.SellingPEditText.setText(finalSp);
            binding.AdditionalEdiText.setText(additional);
            // Load the image using Picasso or any other image loading library
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder) // Placeholder image until the actual image is loaded
                    .fit()
                    .centerCrop()
                    .into(binding.profilePhoto);

            int buyingPriceValue = Integer.parseInt(buyingPrice);

            ItemName=itemName;
            ImageUrl=imageUrl;
            BuyingPriceValue=buyingPriceValue;
            Category=category;
            dbUnits= Integer.parseInt(units);
            uploadKey=bUploadKey;
            uid=bUid;
        }
        mStorageRef = FirebaseStorage.getInstance().getReference(uid).child("sales");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(uid).child("sales");
        List<String> categories = Arrays.asList("Mpesa", "Cash", "Other");

        CategoryAdapter categoryAdapter = new CategoryAdapter(requireContext(), categories);
        binding.spinner.setAdapter(categoryAdapter);



binding.buttonNext.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (isFormValid()) {
            uploadFile();
        }
        else {
            Toast.makeText(getContext(), "Please fill all fields/enter valid units", Toast.LENGTH_LONG).show();
        }


    }
});
        return rootView;
    }

    private boolean isFormValid() {
        return  isSellingPriceValid() && isUnitsValid();
    }


    private boolean isSellingPriceValid() {
        String sellingPrice = binding.SellingPEditText.getText().toString().trim();

        try {
            int sellingPriceValue = Integer.parseInt(sellingPrice );
            return sellingPriceValue >= 0;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isUnitsValid() {
        String units = binding.UnitEditText.getText().toString().trim();

        try {
            int unitsValue = Integer.parseInt(units);
            return unitsValue > 0 && unitsValue<= dbUnits;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return false;
    }
    private void uploadFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTimeString = timeFormat.format(new Date());
// Parse the formatted time string to get the Time object
//        Time currentTime = Time.valueOf(currentTimeString);

        String updatedSellingPrice = binding.SellingPEditText.getText().toString().trim();
        String updatedUnits = binding.UnitEditText.getText().toString().trim();

        // Convert the updated selling price and units to integers
        int updatedSellingPriceValue = Integer.parseInt(updatedSellingPrice);
        int updatedUnitsValue = Integer.parseInt(updatedUnits);

        // Create an instance of sale with the retrieved data
        Sale sale = new Sale(ItemName, ImageUrl, BuyingPriceValue, updatedSellingPriceValue, updatedUnitsValue, Category, new Date(), binding.AdditionalEdiText.getText().toString(), binding.spinner.getSelectedItem().toString());

        // Disable the button and show the progress bar
        binding.buttonNext.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Show toast message for upload in progress
        Toast.makeText(getContext(), "Uploading data. Please wait...", Toast.LENGTH_SHORT).show();

        // Upload the data to the Firebase Realtime Database
        String uploadId = mDatabaseRef.child(currentDate).child(Category).push().getKey();
        mDatabaseRef.child(currentDate).child(currentTimeString).child(Category).child(uploadId).setValue(sale)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data uploaded successfully
                        Toast.makeText(getContext(), "Data uploaded successfully", Toast.LENGTH_SHORT).show();

                        // Enable the button and hide the progress bar
                        binding.buttonNext.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to upload data
                        Toast.makeText(getContext(), "Failed to upload data: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        // Enable the button and hide the progress bar
                        binding.buttonNext.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });

        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference(uid).child("uploads").child(sale.getmCategory()).child(uploadKey);
        Map<String, Object> updateData = new HashMap<>();
        newUnits=dbUnits-sale.getmUnits();
        updateData.put("mUnits", newUnits);

        // Update the item data in the database
        itemRef.updateChildren(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data updated successfully
                        Toast.makeText(getContext(), "Units updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the error
                        Toast.makeText(getContext(), "Failed to update units: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}