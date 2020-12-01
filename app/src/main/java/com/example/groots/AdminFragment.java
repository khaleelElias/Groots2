package com.example.groots;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class AdminFragment extends Fragment {

    private View view;
    private ListView listView;
    private List<UsersData> users = new ArrayList<>();
    private ArrayAdapter<UsersData> adapter;
    private Firebase firebase = new Firebase();

    private TextView userNameInput;
    private TextView passwordInput;
    private Button loginButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view = inflater.inflate(R.layout.admin_fragment_layout, container, false);

        userNameInput = view.findViewById(R.id.AdminLoginNameInput);
        passwordInput = view.findViewById(R.id.AdminLoginPasswordInput);
        loginButton = view.findViewById(R.id.AdminLoginLoginButton);
        listView = view.findViewById(R.id.listOfAllUsers);

        toggleVisibility();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginMessage = MainActivity.adminLogin(
                        userNameInput.getText().toString(),
                        passwordInput.getText().toString()
                );
                Toast.makeText(getContext(), loginMessage, Toast.LENGTH_LONG).show();
                toggleVisibility();
            }
        });

        return view;
    }

    public void fetchData(){
        adapter = new ArrayAdapter<UsersData>(view.getContext(),android.R.layout.simple_list_item_1,users);
        listView.setAdapter(adapter);

        firebase.readAllUsers(users,adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), ViewUserHistory.class);
                intent.putExtra("userId", users.get(position).getId());
                startActivity(intent);
            }
        });

    }


    private void toggleVisibility() {
        if (MainActivity.adminIsLoggedIn) {

            Log.d("check","1");
            userNameInput.setVisibility(View.GONE);
            passwordInput.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            fetchData();
        }
    }


}

