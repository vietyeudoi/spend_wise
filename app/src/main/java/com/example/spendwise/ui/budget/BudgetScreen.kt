package com.example.spendwise.ui.budget


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendwise.data.entity.Budget
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.TransactionViewModel



@Composable
fun BudgetScreen(
    budgetVm:BudgetViewModel=viewModel(),
    txVm:TransactionViewModel=viewModel()
){



    val budgets by
    budgetVm.budgets.observeAsState(
        emptyList()
    )


    val categories by
    txVm.allCategories.observeAsState(
        emptyList()
    )



    var showDialog by remember{
        mutableStateOf(false)
    }


    var amount by remember{
        mutableStateOf("")
    }


    var selectedCategory by remember{
        mutableStateOf<Int?>(null)
    }





    Scaffold(

        floatingActionButton = {

            FloatingActionButton(
                onClick={
                    showDialog=true
                }
            ){

                Icon(
                    Icons.Default.Add,
                    null
                )

            }

        }

    ){padding->



        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ){


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ){

                Button(
                    onClick={
                        budgetVm.prevMonth()
                    }
                ){
                    Text("<")
                }



                Text(
                    "${budgetVm.selectedMonth}/${budgetVm.selectedYear}",
                    style =
                        MaterialTheme.typography.titleLarge
                )



                Button(
                    onClick={
                        budgetVm.nextMonth()
                    }
                ){
                    Text(">")
                }

            }





            LazyColumn{

                items(budgets){budget->


                    val name =
                        categories
                            .find {
                                it.id ==
                                        budget.categoryId
                            }
                            ?.name ?: ""



                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ){

                        Column(
                            Modifier.padding(16.dp)
                        ){

                            Text(name)

                            Text(
                                "Giới hạn: ${budget.limitAmount}"
                            )


                        }


                    }


                }


            }


        }



        if(showDialog){


            AlertDialog(

                onDismissRequest={
                    showDialog=false
                },


                title={
                    Text("Thêm ngân sách")
                },


                text={

                    Column{


                        categories
                            .filter{
                                it.type=="expense"
                            }
                            .forEach{

                                Button(
                                    onClick={
                                        selectedCategory =
                                            it.id
                                    }
                                ){

                                    Text(it.name)

                                }


                            }



                        OutlinedTextField(

                            value=amount,

                            onValueChange={
                                amount=it
                            },

                            label={
                                Text("Số tiền")
                            }

                        )

                    }


                },


                confirmButton={


                    TextButton(

                        onClick={


                            if(
                                selectedCategory!=null
                            ){


                                budgetVm.insert(

                                    Budget(

                                        categoryId =
                                            selectedCategory!!,


                                        limitAmount =
                                            amount
                                                .toDoubleOrNull()
                                                ?:0.0,


                                        month =
                                            budgetVm.selectedMonth,


                                        year =
                                            budgetVm.selectedYear

                                    )

                                )


                            }


                            showDialog=false


                        }

                    ){

                        Text("Lưu")

                    }


                }


            )


        }



    }



}