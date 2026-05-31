import type { RechargePackage } from './types'
import {
  getRechargePackagesErrorMessage,
  getRechargePaymentErrorMessage,
  pickDefaultRechargePackageCode,
  toRechargePackageOptions,
  type RechargePackageOption
} from './view-model'

/**
 * 充值页业务状态。
 *
 * recharge 域负责套餐、余额、默认选中策略和提交前业务状态；
 * Page 负责真正创建充值单、创建支付单、拉起微信小程序支付。
 */
export interface RechargePageState {
  balance: { balance: number; totalIncome: number; totalSpend: number } | null
  packages: RechargePackageOption[]
  selectedPackageCode: string
  loading: boolean
  paying: boolean
  errorMessage: string
}

export function createRechargePageState(): RechargePageState {
  return {
    balance: null,
    packages: [],
    selectedPackageCode: '',
    loading: false,
    paying: false,
    errorMessage: ''
  }
}

export function startRechargePageLoading(): Pick<RechargePageState, 'loading' | 'errorMessage'> {
  return {
    loading: true,
    errorMessage: ''
  }
}

export function resolveRechargePage(
  state: RechargePageState,
  balance: { balance: number; totalIncome: number; totalSpend: number },
  packages: RechargePackage[]
): Pick<RechargePageState, 'balance' | 'packages' | 'selectedPackageCode' | 'loading'> {
  const options = toRechargePackageOptions(packages)
  const selectedPackageCode = options.some((item) => item.code === state.selectedPackageCode)
    ? state.selectedPackageCode
    : pickDefaultRechargePackageCode(packages)

  return {
    balance,
    packages: options,
    selectedPackageCode,
    loading: false
  }
}

export function failRechargePageLoading(error: unknown): Pick<RechargePageState, 'errorMessage' | 'loading'> {
  return {
    errorMessage: getRechargePackagesErrorMessage(error),
    loading: false
  }
}

export function selectRechargePackage(code: string): Pick<RechargePageState, 'selectedPackageCode'> {
  return {
    selectedPackageCode: code
  }
}

export function startRechargePayment(): Pick<RechargePageState, 'paying' | 'errorMessage'> {
  return {
    paying: true,
    errorMessage: ''
  }
}

export function failRechargePayment(error: unknown): Pick<RechargePageState, 'errorMessage' | 'paying'> {
  return {
    errorMessage: getRechargePaymentErrorMessage(error),
    paying: false
  }
}

export function finishRechargePayment(): Pick<RechargePageState, 'paying'> {
  return {
    paying: false
  }
}
