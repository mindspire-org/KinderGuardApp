package com.kinderguard.app.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthRepository {

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void register(String name, String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && auth.getCurrentUser() != null) {
                            sendEmailVerification(null);
                            callback.onSuccess(auth.getCurrentUser());
                        } else {
                            callback.onError(messageFor(task));
                        }
                    }
                });
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        callback.onSuccess(auth.getCurrentUser());
                    } else {
                        callback.onError(messageFor(task));
                    }
                });
    }

    public void loginWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        callback.onSuccess(auth.getCurrentUser());
                    } else {
                        callback.onError(messageFor(task));
                    }
                });
    }

    public void sendEmailVerification(SimpleCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onError("No user logged in");
            return;
        }
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (callback == null) return;
            if (task.isSuccessful()) callback.onSuccess();
            else callback.onError(messageFor(task));
        });
    }

    public boolean isEmailVerified() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public void reloadUser(SimpleCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("No user logged in");
            return;
        }
        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) callback.onSuccess();
            else callback.onError(messageFor(task));
        });
    }

    public void sendPasswordReset(String email, SimpleCallback callback) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) callback.onSuccess();
            else callback.onError(messageFor(task));
        });
    }

    public void logout() {
        auth.signOut();
    }

    public void deleteAccount(SimpleCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("No user logged in");
            return;
        }
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) callback.onSuccess();
            else callback.onError(messageFor(task));
        });
    }

    private String messageFor(Task<?> task) {
        if (task.getException() != null && task.getException().getMessage() != null) {
            return task.getException().getMessage();
        }
        return "Authentication failed";
    }
}
