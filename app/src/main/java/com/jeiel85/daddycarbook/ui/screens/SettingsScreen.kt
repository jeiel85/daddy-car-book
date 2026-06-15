package com.jeiel85.daddycarbook.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeiel85.daddycarbook.ui.component.DadsButton
import com.jeiel85.daddycarbook.ui.component.DadsCard
import com.jeiel85.daddycarbook.ui.component.DadsHeader
import com.jeiel85.daddycarbook.ui.navigation.Screen
import com.jeiel85.daddycarbook.ui.theme.OrangeAccent
import com.jeiel85.daddycarbook.ui.theme.SlateBlue
import com.jeiel85.daddycarbook.ui.viewmodel.VehicleViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicles by viewModel.allVehicles.collectAsState()

    Scaffold(
        topBar = {
            DadsHeader(
                title = "앱 설정 및 도움말 ⚙️",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "이전",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // My vehicles manager block
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "등록 차량 관리",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent
                )
            }

            if (vehicles.isEmpty()) {
                item {
                    DadsCard {
                        Text(
                            text = "등록된 차량이 없습니다.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                items(vehicles) { vehicle ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                            .clickable { navController.navigate(Screen.VehicleForm.createRoute(vehicle.id)) }
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = SlateBlue,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = vehicle.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${vehicle.manufacturer} ${vehicle.model} (${vehicle.plateNumber})",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "편집",
                            tint = OrangeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            item {
                DadsButton(
                    text = "+ 새 차량 등록/추가하기",
                    onClick = { navController.navigate(Screen.VehicleForm.createRoute()) },
                    icon = Icons.Default.Add
                )
            }

            // CSV Export Block
            item {
                Text(
                    text = "데이터 보관 및 내보내기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            item {
                DadsCard {
                    Text(
                        "엑셀(Excel) CSV 데이터 내보내기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "스마트폰에 기록한 모든 차량 정보, 주유 영수증, 정비 일지를 Excel에서 열 수 있는 CSV 포맷 파일 조각으로 인코딩하여 가족 이메일, 카카오톡, 또는 파일 드라이브로 즉시 안전하게 백업 전송합니다.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    DadsButton(
                        text = "CSV 엑셀 파일로 카톡/내보내기 💾",
                        onClick = { viewModel.exportDataToCSV(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                        icon = Icons.Default.Share
                    )
                }
            }

            // Reference Info: Standard Replacement Cycles for guidance
            item {
                Text(
                    text = "정비 소모품 권장 자가 점검 기준표",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            item {
                DadsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PresetItem("🚗 엔진오일 교체", "10,000 km 주행 또는 6개월마다 권장")
                        PresetItem("🛞 타이어 위치교환", "20,000 km 주행 또는 스레드 마모 확인")
                        PresetItem("🔋 배터리 자가성능", "3년 ~ 4년 또는 겨울철 수시 점검")
                        PresetItem("🛑 브레이크 패드", "40,000 km 주행 또는 브레이크 제동 상태 점검")
                        PresetItem("🧹 와이퍼 암/블레이드", "6개월 ~ 1년 주기 또는 소음 작동 편중 시")
                        PresetItem("🍃 에어컨/히터 필터", "10,000 km 주행 또는 여름/겨울철 전 필수교체")
                    }
                }
            }

            // App info credits
            item {
                DadsCard(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "가족 차량 관리 노트 v1.0",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "우리 가족이 항상 건강하고 안전하게 소풍과 야외 활동을 다닐 수 있도록, 복잡함을 완전히 걷어내고 큰 글씨로 가장 중요한 것만 똑똑하게 상기해 주는 한글 로컬 차량 장부 앱입니다. 안전제일!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PresetItem(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(desc, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
