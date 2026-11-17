package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DeveloperConstants
import com.example.ui.components.HonoursTopBar

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        HonoursTopBar(
            title = "অ্যাপ ও ডেভেলপার পরিচিতি",
            subtitle = DeveloperConstants.APP_NAME,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon_emblem_1794931529226),
                            contentDescription = DeveloperConstants.APP_NAME,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(DeveloperConstants.APP_NAME, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(DeveloperConstants.APP_SUBTITLE_BN, style = MaterialTheme.typography.bodyMedium)
                            Text("Version ${DeveloperConstants.APP_VERSION}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("একাডেমিক তথ্য", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("University: ${DeveloperConstants.UNIVERSITY}", style = MaterialTheme.typography.bodyLarge)
                        Text("Department: ${DeveloperConstants.DEPARTMENT}", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "জাতীয় বিশ্ববিদ্যালয়ের অনার্স ১ম থেকে ৪র্থ বর্ষের রাষ্ট্রবিজ্ঞান বিভাগের শিক্ষার্থীদের জন্য অধ্যায়ভিত্তিক নোট, বিগত প্রশ্ন, MCQ পরীক্ষা, PDF লাইব্রেরি ও AI সমাধান প্ল্যাটফর্ম।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Permanent Unchangeable Developer Information
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Developer Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("Developer Name: ${DeveloperConstants.DEVELOPER_NAME}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Contact Number: ${DeveloperConstants.DEVELOPER_PHONE}", style = MaterialTheme.typography.bodyLarge)
                        Text("Email: ${DeveloperConstants.DEVELOPER_EMAIL}", style = MaterialTheme.typography.bodyLarge)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${DeveloperConstants.DEVELOPER_PHONE}"))
                                    context.startActivity(dial)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("about_call_developer"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("কল করুন")
                            }
                            OutlinedButton(
                                onClick = {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${DeveloperConstants.DEVELOPER_EMAIL}")).apply {
                                        putExtra(Intent.EXTRA_SUBJECT, "Honours Solution Inquiry")
                                    }
                                    context.startActivity(emailIntent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("about_email_developer"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Email, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("ইমেইল")
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showPrivacyPolicy = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("about_privacy_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PrivacyTip, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("গোপনীয়তা নীতি")
                    }
                    OutlinedButton(
                        onClick = { showTerms = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("about_terms_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Gavel, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("শর্তাবলি")
                    }
                }
            }

            item {
                Text(
                    text = DeveloperConstants.COPYRIGHT,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }

    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            title = { Text("গোপনীয়তা নীতি (Privacy Policy)") },
            text = {
                Text(
                    "১. Honours Solution শিক্ষার্থীদের ব্যক্তিগত পড়াশোনার তথ্য (প্রোফাইল, বুকমার্ক, নোট ও AI চ্যাট ইতিহাস) আপনার ডিভাইসে নিরাপদে সংরক্ষণ করে।\n" +
                        "২. গ্যালারি থেকে ছবি নির্বাচনের জন্য শূন্য-অনুমতি Android Photo Picker ব্যবহৃত হয়; আমরা অপ্রয়োজনীয় স্টোরেজ অনুমতি চাই না।\n" +
                        "৩. ক্যামেরা অনুমতি শুধুমাত্র আপনি AI Question Solver-এ প্রশ্নের ছবি তুলতে চাইলে ব্যবহৃত হয়।\n" +
                        "৪. কোনো গোপনীয় API Key অ্যাপের APK-তে সংরক্ষিত নয়।"
                )
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicy = false }) { Text("বুঝেছি") }
            }
        )
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            title = { Text("ব্যবহারের শর্তাবলি (Terms & Conditions)") },
            text = {
                Text(
                    "১. অ্যাপে প্রদত্ত সকল নোট, প্রশ্নোত্তর ও সাজেশন জাতীয় বিশ্ববিদ্যালয়ের রাষ্ট্রবিজ্ঞান অনার্স শিক্ষার্থীদের একাডেমিক সহায়তার জন্য নির্মিত।\n" +
                        "২. পরীক্ষার সাজেশন কোনো নিশ্চিত প্রশ্নপত্র নয়; সর্বদা মূল পাঠ্যবই ও সিলেবাস অনুসরণ করুন।\n" +
                        "৩. ডেভেলপার পরিচিতি (${DeveloperConstants.DEVELOPER_NAME}, ${DeveloperConstants.DEVELOPER_PHONE}, ${DeveloperConstants.DEVELOPER_EMAIL}) স্থায়ী ও স্বত্বাধিকার সংরক্ষিত।"
                )
            },
            confirmButton = {
                Button(onClick = { showTerms = false }) { Text("সম্মত") }
            }
        )
    }
}
