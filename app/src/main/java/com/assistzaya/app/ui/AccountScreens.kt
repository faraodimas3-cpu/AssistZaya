package com.assistzaya.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.assistzaya.app.AppViewModel
import com.assistzaya.app.R
import com.assistzaya.app.ui.theme.*

@Composable
fun AccountScreen(vm: AppViewModel, nav: NavController) {
    val acc = vm.account
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ZTopBar(stringResource(R.string.account_title), nav)
        if (acc.signedIn) {
            // Perfil
            CardBox {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(ZRed), contentAlignment = Alignment.Center) {
                        Text(acc.name.take(1).uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(acc.email, color = ZMuted)
                    }
                }
            }
            CardBox {
                AccountRow(Icons.Filled.PlaylistAddCheck, stringResource(R.string.my_list)) { nav.navigate(Routes.list("mylist")) }
                AccountRow(Icons.Filled.Favorite, stringResource(R.string.favorites)) { nav.navigate(Routes.list("favorites")) }
                AccountRow(Icons.Filled.History, stringResource(R.string.history)) { nav.navigate(Routes.list("history")) }
            }
            CardBox {
                AccountRow(Icons.Filled.Settings, stringResource(R.string.settings_title)) { nav.navigate(Routes.SETTINGS) }
                AccountRow(Icons.Filled.SupportAgent, stringResource(R.string.support)) { nav.navigate(Routes.SUPPORT) }
                AccountRow(Icons.Filled.Lock, stringResource(R.string.privacy)) { nav.navigate(Routes.PRIVACY) }
                AccountRow(Icons.Filled.Description, stringResource(R.string.terms)) { nav.navigate(Routes.TERMS) }
            }
            OutlinedButton(
                onClick = { vm.signOut() }, shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(14.dp).height(52.dp)
            ) { Text(stringResource(R.string.sign_out), color = Color.White) }
        } else {
            SignInCard(vm)
            CardBox {
                AccountRow(Icons.Filled.Settings, stringResource(R.string.settings_title)) { nav.navigate(Routes.SETTINGS) }
                AccountRow(Icons.Filled.SupportAgent, stringResource(R.string.support)) { nav.navigate(Routes.SUPPORT) }
                AccountRow(Icons.Filled.Lock, stringResource(R.string.privacy)) { nav.navigate(Routes.PRIVACY) }
                AccountRow(Icons.Filled.Description, stringResource(R.string.terms)) { nav.navigate(Routes.TERMS) }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SignInCard(vm: AppViewModel) {
    var creating by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    CardBox {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.account_signed_out_msg), color = ZMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !creating, onClick = { creating = false }, label = { Text(stringResource(R.string.sign_in)) })
                FilterChip(selected = creating, onClick = { creating = true }, label = { Text(stringResource(R.string.create_account)) })
            }
            if (creating) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.email)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                password, { password = it }, label = { Text(stringResource(R.string.password)) }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    // Demonstração local: sem servidor, a senha NÃO é guardada nem verificada.
                    // Troque por chamada ao seu backend de autenticação.
                    if (email.isBlank() || password.isBlank() || (creating && name.isBlank())) vm.messages.tryEmit(R.string.fill_fields)
                    else vm.signIn(name, email.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZRed), shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(stringResource(if (creating) R.string.create_account else R.string.sign_in), fontWeight = FontWeight.Bold) }
            OutlinedButton(
                onClick = { vm.messages.tryEmit(R.string.google_unavailable) }, shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(stringResource(R.string.continue_google), color = Color.White) }
        }
    }
}

@Composable
private fun AccountRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ZRed)
        Text(label, Modifier.weight(1f).padding(start = 14.dp), fontSize = 16.sp)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = ZMuted)
    }
}

@Composable
fun SupportScreen(nav: NavController) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(stringResource(R.string.support), onBack = { nav.popBackStack() })
        CardBox {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(Icons.Filled.SupportAgent, 48.dp)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(stringResource(R.string.support_whatsapp), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("+258 878 632 350", color = ZMuted)
                    }
                }
                Text(stringResource(R.string.support_sub), color = ZMuted)
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/258878632350")))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZRed), shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text(stringResource(R.string.support_open), fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun PrivacyScreen(vm: AppViewModel, nav: NavController) {
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(stringResource(R.string.privacy), onBack = { nav.popBackStack() })
        Column(Modifier.verticalScroll(rememberScrollState())) {
            LegalBlock(stringResource(R.string.privacy_policy), stringResource(R.string.privacy_policy_text))
            LegalBlock(stringResource(R.string.data_collection), stringResource(R.string.data_collection_text))
            CardBox {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.data_controls), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    OutlinedButton(onClick = { confirm = true }, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Delete, null, tint = ZRed)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.delete_my_data), color = Color.White)
                    }
                }
            }
            TextButton(onClick = { nav.navigate(Routes.TERMS) }, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(stringResource(R.string.terms), color = ZRed)
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false }, containerColor = ZSurface,
            title = { Text(stringResource(R.string.delete_my_data)) },
            text = { Text(stringResource(R.string.delete_confirm), color = ZMuted) },
            confirmButton = { TextButton(onClick = { vm.deleteMyData(); confirm = false }) { Text(stringResource(R.string.confirm), color = ZRed) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel), color = ZMuted) } }
        )
    }
}

@Composable
fun TermsScreen(nav: NavController) {
    Column(Modifier.fillMaxSize()) {
        SimpleTopBar(stringResource(R.string.terms), onBack = { nav.popBackStack() })
        Column(Modifier.verticalScroll(rememberScrollState())) {
            LegalBlock(stringResource(R.string.terms), stringResource(R.string.terms_text))
        }
    }
}

@Composable
private fun LegalBlock(title: String, body: String) {
    CardBox {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(body, color = ZMuted, fontSize = 15.sp)
        }
    }
}
