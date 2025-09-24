package com.example.w03

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.w03.ui.theme.ComposeLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeLabTheme {
                HomeScreen()
            }
        }
    }
}

data class Message(val name: String, val msg: String)
data class Profile(val name: String, val intro: String)

@Composable
fun HomeScreen() {
    Surface {
        // Box 대신 Column을 사용합니다.
        Column(
            modifier = Modifier.fillMaxSize(), // 전체 화면을 차지
            verticalArrangement = Arrangement.Center, // 자식들을 세로 방향으로 중앙 정렬
            horizontalAlignment = Alignment.CenterHorizontally // 자식들을 가로 방향으로 중앙 정렬
        ) {
            // 이제 카드들이 세로로 순서대로 배치됩니다.
            ProfileCard(Profile("장성훈", "평범하게 살자"))
            MessageCard(Message("Android", "Jetpack Compose"))
        }
    }
}



@Preview(
    name = "Profile Card Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun PreviewProfileCard() {
    ComposeLabTheme {
        ProfileCard(Profile("장성훈", "평범하게 살자"))
    }
}

@Preview(
    name = "Message Card Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun PreviewMessageCard() {
    ComposeLabTheme {
        MessageCard(Message("Android", "Jetpack Compose"))
    }
}




@Composable
fun MessageCard(me: Message) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),

        modifier = Modifier.padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = me.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = me.msg,
            style = MaterialTheme.typography.titleLarge
            )
        }
    }
}


@Composable
fun ProfileCard(data: Profile) {
    // 1. Card 컴포저블로 전체를 감쌉니다.
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),

        // 2. MessageCard와 동일하게 border 속성을 추가합니다.
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),

        // 카드 바깥쪽 여백
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            // 카드 안쪽 내용물과 테두리 사이의 여백
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.profile_picture),
                contentDescription = "연락처 프로필 사진",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = data.name,
                    color = MaterialTheme.colorScheme.onSurface, // onBackground 대신 onSurface 추천
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp)) // 간격 살짝 조정
                Text(
                    text = data.intro,
                    color = MaterialTheme.colorScheme.onSurface, // onBackground 대신 onSurface 추천
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
