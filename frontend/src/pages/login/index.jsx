import styles from "@/styles/Login.module.css"
import { useRouter } from "next/router"
import { useState } from "react"

export default function LoginForm() {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const router = useRouter()

  async function login() {
    try {
      const response = await fetch("http://localhost:8080/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username,
          password,
        }),
      })

      if (!response.ok) {
        throw new Error("Erro ao fazer login")
      }

      const data = await response.json()

      if (data.accessToken) {
        localStorage.setItem("token", data.accessToken)
        router.push("/feed")
      } else {
        console.error("Token não recebido")
      }
    } catch (error) {
      console.error("Erro durante o login:", error)
    }
  }

  return (
    <div className={styles.pageLogin}>
      <h1>Tela de Login</h1>

      <div className={styles.loginContainer}>
        <div className={styles.formLogin}>
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
        <button onClick={login}>Login</button>
      </div>
    </div>
  )
}
