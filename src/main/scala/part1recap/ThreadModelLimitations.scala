package part1recap

object ThreadModelLimitations extends App{

  /**
   * #1: OOP encapsulation is only valid in single threaded model
   */

  class BankAcc(private var amount: Int) {
    override def toString: String = "" + amount
    def getBalance: Int = amount
    def withdraw(money: Int): Unit = this.amount -= money
    def deposit(money: Int): Unit = this.amount += money

    def safeWithdraw(money: Int): Unit = this.synchronized {
      this.amount -= money
    }
  }

  val acc = new BankAcc(2000)
  for (_ <- 1 to 1000) {
    new Thread(() => acc.withdraw(1)).start()
  }

  for (_ <- 1 to 1000) {
    new Thread(() => acc.deposit(1)).start()
  }

  println(acc.getBalance) // 1999

  // OOP encapsulation is broken in a multithreaded env => synchronization
  // synchronization is very slow and causes different problems (deadlocks, livelocks)

  /**
   *  #2: more than one signal at the time? many background tasks and threads?
   *  who gave the signal? what if the signal throws Exception? Many questions..
   */

  /**
   *  #3: tracing and dealing with errors is HARD, gets HARDER in bigger apps / projects
   */




}
