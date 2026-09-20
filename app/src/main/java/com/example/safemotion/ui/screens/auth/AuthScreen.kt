package com.example.safemotion.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.safemotion.R

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onModeChange: (AuthMode) -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))

            if (uiState.mode == AuthMode.Recover) {
                Text(
                    text = stringResource(R.string.recover_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(R.string.recover_description),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.mode == AuthMode.SignIn) {
                        Button(onClick = { onModeChange(AuthMode.SignIn) }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.sign_in))
                        }
                    } else {
                        OutlinedButton(onClick = { onModeChange(AuthMode.SignIn) }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.sign_in))
                        }
                    }
                    if (uiState.mode == AuthMode.Register) {
                        Button(onClick = { onModeChange(AuthMode.Register) }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.register))
                        }
                    } else {
                        OutlinedButton(onClick = { onModeChange(AuthMode.Register) }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.register))
                        }
                    }
                }
            }

            if (uiState.mode == AuthMode.Register) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.full_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = onPhoneChange,
                    label = { Text(stringResource(R.string.phone)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.mode != AuthMode.Recover) {
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(stringResource(if (passwordVisible) R.string.hide_password else R.string.show_password))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            uiState.message?.let { message ->
                Text(
                    text = stringResource(
                        when (message) {
                            AuthMessage.MissingFields -> R.string.error_missing_fields
                            AuthMessage.InvalidEmail -> R.string.error_invalid_email
                            AuthMessage.ShortPassword -> R.string.error_short_password
                            AuthMessage.InvalidCredentials -> R.string.error_invalid_credentials
                            AuthMessage.ExistingEmail -> R.string.error_existing_email
                            AuthMessage.RecoverySent -> R.string.recovery_sent
                        }
                    ),
                    color = if (message == AuthMessage.RecoverySent) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(
                        when (uiState.mode) {
                            AuthMode.SignIn -> R.string.sign_in
                            AuthMode.Register -> R.string.create_account
                            AuthMode.Recover -> R.string.send_recovery_link
                        }
                    )
                )
            }

            if (uiState.mode == AuthMode.SignIn) {
                TextButton(onClick = { onModeChange(AuthMode.Recover) }) {
                    Text(stringResource(R.string.forgot_password))
                }
                Text(
                    text = stringResource(R.string.demo_account),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (uiState.mode == AuthMode.Recover) {
                TextButton(onClick = { onModeChange(AuthMode.SignIn) }) {
                    Text(stringResource(R.string.back_to_sign_in))
                }
            }
        }
    }
}
