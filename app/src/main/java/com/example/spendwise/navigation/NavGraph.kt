package com.example.spendwise.navigation
//chịu trách nhiệm điều hướng
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.spendwise.ui.add.AddTransactionScreen
import com.example.spendwise.ui.budget.BudgetScreen
import com.example.spendwise.ui.history.HistoryScreen
import com.example.spendwise.ui.home.HomeScreen
import com.example.spendwise.ui.stats.StatsScreen
import com.example.spendwise.ui.detail.TransactionDetailScreen
sealed class Screen(val route: String) {

    object Home : Screen("home")

    object AddTransaction : Screen("add_transaction")

    object History : Screen("history/{categoryId}") {
        fun createRoute(categoryId: Int) = "history/$categoryId"
        const val ARG_CATEGORY_ID = "categoryId"
    }

    object Stats : Screen("stats")

    object Budget : Screen("budget")

    object EditTransaction :
        Screen("edit_transaction/{transactionId}") {

        fun createRoute(id: Int) =
            "edit_transaction/$id"

        const val ARG_ID = "transactionId"
    }

    object TransactionDetail :
        Screen("transaction_detail/{transactionId}") {

        fun createRoute(id: Int) =
            "transaction_detail/$id"

        const val ARG_ID = "transactionId"
    }
}

@Composable
fun SpendWiseNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        //NavHost khởi động khi mở app
        startDestination = Screen.Home.route,
        modifier         = modifier
    ) {
        composable(Screen.Home.route) {
            // nút thêm, truyền xuống home
            HomeScreen(
                onNavigateToAdd = {
                    //khi click thì dòng này thực hiện
                navController.navigate(Screen.AddTransaction.route)
            })
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                transactionId  = -1,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.EditTransaction.route,
            arguments = listOf(navArgument(Screen.EditTransaction.ARG_ID) {
                type         = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt(Screen.EditTransaction.ARG_ID) ?: -1
            AddTransactionScreen(
                //luồng nút chỉnh sửa
                transactionId  = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.History.route,
            arguments = listOf(navArgument(Screen.History.ARG_CATEGORY_ID) {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt(Screen.History.ARG_CATEGORY_ID) ?: -1
            HistoryScreen(
                navController = navController,
                categoryId = categoryId
            )
        }
        composable(
            route = Screen.TransactionDetail.route ,
            arguments = listOf(

                navArgument(Screen.TransactionDetail.ARG_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val id =
                backStackEntry.arguments?.getInt(
                    Screen.TransactionDetail.ARG_ID
                ) ?: -1

            TransactionDetailScreen(
                transactionId = id,
                onBack = {
                    navController.popBackStack()
                },
                onEdit = {
                    navController.navigate(
                        Screen.EditTransaction.createRoute(id)
                        //mở chỉnh sửa
                    )
                }
            )
        }
        composable(Screen.Stats.route)  { StatsScreen() }
        composable(Screen.Budget.route) { BudgetScreen(navController = navController) }
    }
}
