<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Form, Field, ErrorMessage } from 'vee-validate'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

// Sử dụng store
const authStore = useAuthStore()

// State để quản lý các bước của form (login -> otp)
const loginStep = ref<'credentials' | 'otp'>('credentials')
const userEmail = ref('') // Lưu lại email để dùng cho bước OTP

// --- Schema Validation cho bước 1: Đăng nhập ---
const loginSchema = toTypedSchema(
  z.object({
    email: z.string().min(1, 'Email không được để trống').email('Email không hợp lệ'),
    password: z.string().min(1, 'Mật khẩu không được để trống'),
  }),
)

// --- Schema Validation cho bước 2: Nhập OTP ---
const otpSchema = toTypedSchema(
  z.object({
    otp: z.string().min(6, 'OTP phải có 6 chữ số').max(6, 'OTP phải có 6 chữ số'),
  }),
)

// Hàm xử lý khi submit form đăng nhập
async function handleLogin(values: any) {
  userEmail.value = values.email
  const otp = await authStore.login(values.email, values.password)
  
  // Nếu API login trả về OTP thành công, chuyển sang bước nhập OTP
  if (otp) {
    loginStep.value = 'otp'
    // In OTP ra console để tiện debug
    console.log('Your OTP is:', otp)
  }
}

// Hàm xử lý khi submit form OTP
async function handleVerifyOtp(values: any) {
  await authStore.verifyOtp(userEmail.value, values.otp)
}
</script>

<template>
  <Card class="w-full max-w-sm">
    <CardHeader>
      <CardTitle class="text-2xl">
        Đăng nhập
      </CardTitle>
      <CardDescription>
        Nhập email và mật khẩu của bạn để tiếp tục.
      </CardDescription>
    </CardHeader>
    <CardContent>
      <!-- FORM ĐĂNG NHẬP -->
      <Form v-if="loginStep === 'credentials'" :validation-schema="loginSchema" @submit="handleLogin" class="grid gap-4">
        <div class="grid gap-2">
          <Label for="email">Email</Label>
          <Field name="email" v-slot="{ field }">
            <Input id="email" type="email" placeholder="m@example.com" v-bind="field" />
          </Field>
          <ErrorMessage name="email" class="text-red-500 text-sm" />
        </div>
        <div class="grid gap-2">
          <Label for="password">Mật khẩu</Label>
          <Field name="password" v-slot="{ field }">
            <Input id="password" type="password" v-bind="field" />
          </Field>
          <ErrorMessage name="password" class="text-red-500 text-sm" />
        </div>

        <!-- Hiển thị lỗi từ API -->
        <p v-if="authStore.error && loginStep === 'credentials'" class="text-red-500 text-sm text-center">
          {{ authStore.error }}
        </p>

        <Button type="submit" class="w-full" :disabled="authStore.isLoading">
          <span v-if="authStore.isLoading">Đang xử lý...</span>
          <span v-else>Đăng nhập</span>
        </Button>
      </Form>

      <!-- FORM NHẬP OTP -->
      <Form v-else :validation-schema="otpSchema" @submit="handleVerifyOtp" class="grid gap-4">
        <div class="grid gap-2 text-center">
          <Label for="otp">Xác thực OTP</Label>
          <p class="text-sm text-muted-foreground">
            Một mã OTP đã được tạo. Vui lòng nhập mã để hoàn tất đăng nhập.
          </p>
          <Field name="otp" v-slot="{ field }">
            <Input id="otp" placeholder="123456" v-bind="field" class="text-center tracking-[0.5em]" />
          </Field>
          <ErrorMessage name="otp" class="text-red-500 text-sm" />
        </div>

        <!-- Hiển thị lỗi từ API -->
        <p v-if="authStore.error && loginStep === 'otp'" class="text-red-500 text-sm text-center">
          {{ authStore.error }}
        </p>

        <Button type="submit" class="w-full" :disabled="authStore.isLoading">
          <span v-if="authStore.isLoading">Đang xác thực...</span>
          <span v-else>Xác thực</span>
        </Button>
      </Form>

      <div class="mt-4 text-center text-sm">
        Chưa có tài khoản?
        <router-link :to="{ name: 'Register' }" class="underline">
          Đăng ký
        </router-link>
      </div>
    </CardContent>
  </Card>
</template>