	.text
	.file	"llvm-link"
	.globl	functie_voor_datastructuren     # -- Begin function functie_voor_datastructuren
	.p2align	4, 0x90
	.type	functie_voor_datastructuren,@function
functie_voor_datastructuren:            # @functie_voor_datastructuren
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movq	%rdi, -8(%rbp)
	movabsq	$.L.str, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-8(%rbp), %rax
	movl	$0, (%rax)
	movq	-8(%rbp), %rax
	xorps	%xmm0, %xmm0
	movss	%xmm0, 4(%rax)
	movq	-8(%rbp), %rax
	movq	$0, 8(%rax)
	movabsq	$.L.str.1, %rdi
	movb	$0, %al
	callq	printf@PLT
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end0:
	.size	functie_voor_datastructuren, .Lfunc_end0-functie_voor_datastructuren
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_0                   # -- Begin function FF_function_0
	.p2align	4, 0x90
	.type	FF_function_0,@function
FF_function_0:                          # @FF_function_0
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movq	%rdi, -8(%rbp)
	movabsq	$.L.str.2, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.1.3, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end1:
	.size	FF_function_0, .Lfunc_end1-FF_function_0
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_1                   # -- Begin function FF_function_1
	.p2align	4, 0x90
	.type	FF_function_1,@function
FF_function_1:                          # @FF_function_1
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movw	%di, -4(%rbp)
	movsd	%xmm0, -24(%rbp)
	movq	%rsi, -16(%rbp)
	movw	%dx, -2(%rbp)
	movabsq	$.L.str.2.4, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.3, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end2:
	.size	FF_function_1, .Lfunc_end2-FF_function_1
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_2                   # -- Begin function FF_function_2
	.p2align	4, 0x90
	.type	FF_function_2,@function
FF_function_2:                          # @FF_function_2
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movw	%di, -4(%rbp)
	movw	%si, -2(%rbp)
	movabsq	$.L.str.4, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.5, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end3:
	.size	FF_function_2, .Lfunc_end3-FF_function_2
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_3                   # -- Begin function FF_function_3
	.p2align	4, 0x90
	.type	FF_function_3,@function
FF_function_3:                          # @FF_function_3
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movss	%xmm0, -4(%rbp)
	movabsq	$.L.str.6, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.7, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end4:
	.size	FF_function_3, .Lfunc_end4-FF_function_3
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_4                   # -- Begin function FF_function_4
	.p2align	4, 0x90
	.type	FF_function_4,@function
FF_function_4:                          # @FF_function_4
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movw	%di, -2(%rbp)
	movq	%rsi, -32(%rbp)
	movsd	%xmm0, -24(%rbp)
	movq	%rdx, -16(%rbp)
	movabsq	$.L.str.8, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.9, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end5:
	.size	FF_function_4, .Lfunc_end5-FF_function_4
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_5                   # -- Begin function FF_function_5
	.p2align	4, 0x90
	.type	FF_function_5,@function
FF_function_5:                          # @FF_function_5
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movb	%dil, -1(%rbp)
	movq	%rsi, -16(%rbp)
	movabsq	$.L.str.10, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.11, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end6:
	.size	FF_function_5, .Lfunc_end6-FF_function_5
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_6                   # -- Begin function FF_function_6
	.p2align	4, 0x90
	.type	FF_function_6,@function
FF_function_6:                          # @FF_function_6
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movq	32(%rbp), %rax
	movq	24(%rbp), %rax
	movq	16(%rbp), %rax
	movw	%di, -6(%rbp)
	movb	%sil, -3(%rbp)
	movq	%rdx, -32(%rbp)
	movb	%cl, -2(%rbp)
	movss	%xmm0, -16(%rbp)
	movq	%r8, -24(%rbp)
	movb	%r9b, -1(%rbp)
	movss	%xmm1, -12(%rbp)
	movabsq	$.L.str.12, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.13, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end7:
	.size	FF_function_6, .Lfunc_end7-FF_function_6
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_7                   # -- Begin function FF_function_7
	.p2align	4, 0x90
	.type	FF_function_7,@function
FF_function_7:                          # @FF_function_7
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movq	%rdi, -8(%rbp)
	movabsq	$.L.str.14, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.15, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end8:
	.size	FF_function_7, .Lfunc_end8-FF_function_7
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_8                   # -- Begin function FF_function_8
	.p2align	4, 0x90
	.type	FF_function_8,@function
FF_function_8:                          # @FF_function_8
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movq	%rdi, -16(%rbp)
	movq	%rsi, -8(%rbp)
	movabsq	$.L.str.16, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.17, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end9:
	.size	FF_function_8, .Lfunc_end9-FF_function_8
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_9                   # -- Begin function FF_function_9
	.p2align	4, 0x90
	.type	FF_function_9,@function
FF_function_9:                          # @FF_function_9
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movb	16(%rbp), %al
	movss	%xmm0, -20(%rbp)
	movb	%dil, -3(%rbp)
	movb	%sil, -2(%rbp)
	movq	%rdx, -48(%rbp)
	movb	%cl, -1(%rbp)
	movsd	%xmm1, -40(%rbp)
	movss	%xmm2, -16(%rbp)
	movq	%r8, -32(%rbp)
	movss	%xmm3, -12(%rbp)
	movw	%r9w, -6(%rbp)
	movabsq	$.L.str.18, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.19, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end10:
	.size	FF_function_9, .Lfunc_end10-FF_function_9
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_11                  # -- Begin function FF_function_11
	.p2align	4, 0x90
	.type	FF_function_11,@function
FF_function_11:                         # @FF_function_11
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$208, %rsp
	testb	%al, %al
	je	.LBB11_5
# %bb.4:
	movaps	%xmm1, -144(%rbp)
	movaps	%xmm2, -128(%rbp)
	movaps	%xmm3, -112(%rbp)
	movaps	%xmm4, -96(%rbp)
	movaps	%xmm5, -80(%rbp)
	movaps	%xmm6, -64(%rbp)
	movaps	%xmm7, -48(%rbp)
.LBB11_5:
	movq	%r9, -168(%rbp)
	movq	%r8, -176(%rbp)
	movq	%rcx, -184(%rbp)
	movq	%rdx, -192(%rbp)
	movq	%rsi, -200(%rbp)
	movq	%rdi, -208(%rbp)
	movss	%xmm0, -8(%rbp)
	movabsq	$.L.str.20, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-32(%rbp), %rax
	leaq	-208(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$64, 4(%rax)
	movl	$0, (%rax)
	leaq	-32(%rbp), %rcx
	movl	-32(%rbp), %edx
	cmpl	$40, %edx
	ja	.LBB11_2
# %bb.1:
	movslq	%edx, %rax
	addq	16(%rcx), %rax
	addl	$8, %edx
	movl	%edx, (%rcx)
	jmp	.LBB11_3
.LBB11_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$8, %rdx
	movq	%rdx, 8(%rcx)
.LBB11_3:
	movb	(%rax), %al
	movb	%al, -1(%rbp)
	xorps	%xmm0, %xmm0
	movl	$1, %edi
	movb	$1, %al
	callq	FF_function_11
	xorps	%xmm0, %xmm0
	movl	$1, %edi
	movl	$2, %esi
	movl	$3, %edx
	movb	$1, %al
	callq	FF_function_11
	movl	$.L.str.21, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movb	-1(%rbp), %al
	addq	$208, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end11:
	.size	FF_function_11, .Lfunc_end11-FF_function_11
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_13                  # -- Begin function FF_function_13
	.p2align	4, 0x90
	.type	FF_function_13,@function
FF_function_13:                         # @FF_function_13
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$256, %rsp                      # imm = 0x100
	testb	%al, %al
	je	.LBB12_5
# %bb.4:
	movaps	%xmm0, -208(%rbp)
	movaps	%xmm1, -192(%rbp)
	movaps	%xmm2, -176(%rbp)
	movaps	%xmm3, -160(%rbp)
	movaps	%xmm4, -144(%rbp)
	movaps	%xmm5, -128(%rbp)
	movaps	%xmm6, -112(%rbp)
	movaps	%xmm7, -96(%rbp)
.LBB12_5:
	movq	%r9, -216(%rbp)
	movq	%r8, -224(%rbp)
	movq	%rcx, -232(%rbp)
	movq	%rdx, -240(%rbp)
	movq	%rdi, -80(%rbp)
	movq	%rsi, -72(%rbp)
	movabsq	$.L.str.22, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-64(%rbp), %rax
	leaq	-256(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$48, 4(%rax)
	movl	$16, (%rax)
	leaq	-64(%rbp), %rcx
	movq	%rcx, %rdx
	addq	$4, %rdx
	movl	-60(%rbp), %esi
	cmpl	$160, %esi
	ja	.LBB12_2
# %bb.1:
	movslq	%esi, %rax
	addq	16(%rcx), %rax
	addl	$16, %esi
	movl	%esi, (%rdx)
	jmp	.LBB12_3
.LBB12_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$8, %rdx
	movq	%rdx, 8(%rcx)
.LBB12_3:
	movss	(%rax), %xmm0                   # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -4(%rbp)
	movl	$0, -40(%rbp)
	movl	$1056964608, -36(%rbp)          # imm = 0x3F000000
	movq	$0, -32(%rbp)
	movq	-40(%rbp), %rdi
	movq	-32(%rbp), %rsi
	movl	$1, %edx
	xorl	%eax, %eax
	callq	FF_function_13
	movl	$0, -24(%rbp)
	movl	$1056964608, -20(%rbp)          # imm = 0x3F000000
	movq	$0, -16(%rbp)
	movq	-24(%rbp), %rdi
	movq	-16(%rbp), %rsi
	movl	$1, %edx
	movl	$2, %ecx
	movl	$3, %r8d
	xorl	%eax, %eax
	callq	FF_function_13
	movl	$.L.str.23, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movss	-4(%rbp), %xmm0                 # xmm0 = mem[0],zero,zero,zero
	addq	$256, %rsp                      # imm = 0x100
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end12:
	.size	FF_function_13, .Lfunc_end12-FF_function_13
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_15                  # -- Begin function FF_function_15
	.p2align	4, 0x90
	.type	FF_function_15,@function
FF_function_15:                         # @FF_function_15
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$304, %rsp                      # imm = 0x130
	testb	%al, %al
	je	.LBB13_5
# %bb.4:
	movaps	%xmm0, -256(%rbp)
	movaps	%xmm1, -240(%rbp)
	movaps	%xmm2, -224(%rbp)
	movaps	%xmm3, -208(%rbp)
	movaps	%xmm4, -192(%rbp)
	movaps	%xmm5, -176(%rbp)
	movaps	%xmm6, -160(%rbp)
	movaps	%xmm7, -144(%rbp)
.LBB13_5:
	movq	%r9, -264(%rbp)
	movq	%r8, -272(%rbp)
	movq	%rcx, -280(%rbp)
	movq	%rdx, -288(%rbp)
	movq	%rdi, -128(%rbp)
	movq	%rsi, -120(%rbp)
	movabsq	$.L.str.24, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-80(%rbp), %rax
	leaq	-304(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$48, 4(%rax)
	movl	$16, (%rax)
	leaq	-80(%rbp), %rcx
	movl	-80(%rbp), %edx
	cmpl	$32, %edx
	ja	.LBB13_2
# %bb.1:
	movslq	%edx, %rax
	addq	16(%rcx), %rax
	addl	$16, %edx
	movl	%edx, (%rcx)
	jmp	.LBB13_3
.LBB13_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$16, %rdx
	movq	%rdx, 8(%rcx)
.LBB13_3:
	movups	(%rax), %xmm0
	movaps	%xmm0, -48(%rbp)
	movl	$0, -32(%rbp)
	movl	$1056964608, -28(%rbp)          # imm = 0x3F000000
	movq	$0, -24(%rbp)
	movq	-32(%rbp), %rdi
	movq	-24(%rbp), %rsi
	movl	$1, %edx
	xorl	%eax, %eax
	callq	FF_function_15
	movq	%rax, -112(%rbp)
	movq	%rdx, -104(%rbp)
	movl	$0, -16(%rbp)
	movl	$1056964608, -12(%rbp)          # imm = 0x3F000000
	movq	$0, -8(%rbp)
	movq	-16(%rbp), %rdi
	movq	-8(%rbp), %rsi
	movl	$1, %edx
	movl	$2, %ecx
	movl	$3, %r8d
	xorl	%eax, %eax
	callq	FF_function_15
	movq	%rax, -96(%rbp)
	movq	%rdx, -88(%rbp)
	movl	$.L.str.25, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movq	-48(%rbp), %rax
	movq	-40(%rbp), %rdx
	addq	$304, %rsp                      # imm = 0x130
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end13:
	.size	FF_function_15, .Lfunc_end13-FF_function_15
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_23                  # -- Begin function FF_function_23
	.p2align	4, 0x90
	.type	FF_function_23,@function
FF_function_23:                         # @FF_function_23
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$208, %rsp
	testb	%al, %al
	je	.LBB14_5
# %bb.4:
	movaps	%xmm1, -144(%rbp)
	movaps	%xmm2, -128(%rbp)
	movaps	%xmm3, -112(%rbp)
	movaps	%xmm4, -96(%rbp)
	movaps	%xmm5, -80(%rbp)
	movaps	%xmm6, -64(%rbp)
	movaps	%xmm7, -48(%rbp)
.LBB14_5:
	movq	%r9, -168(%rbp)
	movq	%r8, -176(%rbp)
	movq	%rcx, -184(%rbp)
	movq	%rdx, -192(%rbp)
	movq	%rsi, -200(%rbp)
	movq	%rdi, -208(%rbp)
	movss	%xmm0, -8(%rbp)
	movabsq	$.L.str.26, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-32(%rbp), %rax
	leaq	-208(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$64, 4(%rax)
	movl	$0, (%rax)
	leaq	-32(%rbp), %rcx
	movl	-32(%rbp), %edx
	cmpl	$40, %edx
	ja	.LBB14_2
# %bb.1:
	movslq	%edx, %rax
	addq	16(%rcx), %rax
	addl	$8, %edx
	movl	%edx, (%rcx)
	jmp	.LBB14_3
.LBB14_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$8, %rdx
	movq	%rdx, 8(%rcx)
.LBB14_3:
	movb	(%rax), %al
	movb	%al, -1(%rbp)
	xorps	%xmm0, %xmm0
	movl	$1, %edi
	movb	$1, %al
	callq	FF_function_23
	xorps	%xmm0, %xmm0
	movl	$1, %edi
	movl	$2, %esi
	movl	$3, %edx
	movb	$1, %al
	callq	FF_function_23
	movl	$.L.str.27, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movb	-1(%rbp), %al
	addq	$208, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end14:
	.size	FF_function_23, .Lfunc_end14-FF_function_23
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_25                  # -- Begin function FF_function_25
	.p2align	4, 0x90
	.type	FF_function_25,@function
FF_function_25:                         # @FF_function_25
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$240, %rsp
	testb	%al, %al
	je	.LBB15_5
# %bb.4:
	movaps	%xmm0, -192(%rbp)
	movaps	%xmm1, -176(%rbp)
	movaps	%xmm2, -160(%rbp)
	movaps	%xmm3, -144(%rbp)
	movaps	%xmm4, -128(%rbp)
	movaps	%xmm5, -112(%rbp)
	movaps	%xmm6, -96(%rbp)
	movaps	%xmm7, -80(%rbp)
.LBB15_5:
	movq	%r9, -200(%rbp)
	movq	%r8, -208(%rbp)
	movq	%rcx, -216(%rbp)
	movq	%rdx, -224(%rbp)
	movq	%rsi, -232(%rbp)
	movw	%di, -24(%rbp)
	movabsq	$.L.str.28, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-64(%rbp), %rax
	leaq	-240(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$48, 4(%rax)
	movl	$8, (%rax)
	leaq	-64(%rbp), %rcx
	movq	%rcx, %rdx
	addq	$4, %rdx
	movl	-60(%rbp), %esi
	cmpl	$160, %esi
	ja	.LBB15_2
# %bb.1:
	movslq	%esi, %rax
	addq	16(%rcx), %rax
	addl	$16, %esi
	movl	%esi, (%rdx)
	jmp	.LBB15_3
.LBB15_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$8, %rdx
	movq	%rdx, 8(%rcx)
.LBB15_3:
	movss	(%rax), %xmm0                   # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -28(%rbp)
	movw	$0, -16(%rbp)
	movzwl	-16(%rbp), %edi
	movl	$1, %esi
	xorl	%eax, %eax
	callq	FF_function_25
	movw	$0, -8(%rbp)
	movzwl	-8(%rbp), %edi
	movl	$1, %esi
	movl	$2, %edx
	movl	$3, %ecx
	xorl	%eax, %eax
	callq	FF_function_25
	movl	$.L.str.29, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movss	-28(%rbp), %xmm0                # xmm0 = mem[0],zero,zero,zero
	addq	$240, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end15:
	.size	FF_function_25, .Lfunc_end15-FF_function_25
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_36                  # -- Begin function FF_function_36
	.p2align	4, 0x90
	.type	FF_function_36,@function
FF_function_36:                         # @FF_function_36
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$224, %rsp
	testb	%al, %al
	je	.LBB16_5
# %bb.4:
	movaps	%xmm0, -176(%rbp)
	movaps	%xmm1, -160(%rbp)
	movaps	%xmm2, -144(%rbp)
	movaps	%xmm3, -128(%rbp)
	movaps	%xmm4, -112(%rbp)
	movaps	%xmm5, -96(%rbp)
	movaps	%xmm6, -80(%rbp)
	movaps	%xmm7, -64(%rbp)
.LBB16_5:
	movq	%r9, -184(%rbp)
	movq	%r8, -192(%rbp)
	movq	%rcx, -200(%rbp)
	movq	%rdx, -208(%rbp)
	movq	%rsi, -216(%rbp)
	movb	%dil, -9(%rbp)
	movabsq	$.L.str.30, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-48(%rbp), %rax
	leaq	-224(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$48, 4(%rax)
	movl	$8, (%rax)
	leaq	-48(%rbp), %rcx
	movl	-48(%rbp), %edx
	cmpl	$40, %edx
	ja	.LBB16_2
# %bb.1:
	movslq	%edx, %rax
	addq	16(%rcx), %rax
	addl	$8, %edx
	movl	%edx, (%rcx)
	jmp	.LBB16_3
.LBB16_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$8, %rdx
	movq	%rdx, 8(%rcx)
.LBB16_3:
	movw	(%rax), %ax
	movw	%ax, -8(%rbp)
	xorl	%edi, %edi
	movl	$1, %esi
	xorl	%eax, %eax
	callq	FF_function_36
	movw	%ax, -24(%rbp)
	xorl	%edi, %edi
	movl	$1, %esi
	movl	$2, %edx
	movl	$3, %ecx
	xorl	%eax, %eax
	callq	FF_function_36
	movw	%ax, -16(%rbp)
	movl	$.L.str.31, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movw	-8(%rbp), %ax
	addq	$224, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end16:
	.size	FF_function_36, .Lfunc_end16-FF_function_36
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_42                  # -- Begin function FF_function_42
	.p2align	4, 0x90
	.type	FF_function_42,@function
FF_function_42:                         # @FF_function_42
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$224, %rsp
	testb	%al, %al
	je	.LBB17_5
# %bb.4:
	movaps	%xmm0, -176(%rbp)
	movaps	%xmm1, -160(%rbp)
	movaps	%xmm2, -144(%rbp)
	movaps	%xmm3, -128(%rbp)
	movaps	%xmm4, -112(%rbp)
	movaps	%xmm5, -96(%rbp)
	movaps	%xmm6, -80(%rbp)
	movaps	%xmm7, -64(%rbp)
.LBB17_5:
	movq	%r9, -184(%rbp)
	movq	%r8, -192(%rbp)
	movq	%rcx, -200(%rbp)
	movq	%rdx, -208(%rbp)
	movq	%rsi, -216(%rbp)
	movq	%rdi, -40(%rbp)
	movabsq	$.L.str.32, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-32(%rbp), %rax
	leaq	-224(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$48, 4(%rax)
	movl	$8, (%rax)
	leaq	-32(%rbp), %rcx
	movl	-32(%rbp), %edx
	cmpl	$40, %edx
	ja	.LBB17_2
# %bb.1:
	movslq	%edx, %rax
	addq	16(%rcx), %rax
	addl	$8, %edx
	movl	%edx, (%rcx)
	jmp	.LBB17_3
.LBB17_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$8, %rdx
	movq	%rdx, 8(%rcx)
.LBB17_3:
	movq	(%rax), %rax
	movq	%rax, -8(%rbp)
	xorl	%edi, %edi
	movl	$1, %esi
	xorl	%eax, %eax
	callq	FF_function_42
	xorl	%edi, %edi
	movl	$1, %esi
	movl	$2, %edx
	movl	$3, %ecx
	xorl	%eax, %eax
	callq	FF_function_42
	movl	$.L.str.33, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movq	-8(%rbp), %rax
	addq	$224, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end17:
	.size	FF_function_42, .Lfunc_end17-FF_function_42
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_45                  # -- Begin function FF_function_45
	.p2align	4, 0x90
	.type	FF_function_45,@function
FF_function_45:                         # @FF_function_45
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$272, %rsp                      # imm = 0x110
	testb	%al, %al
	je	.LBB18_5
# %bb.4:
	movaps	%xmm1, -208(%rbp)
	movaps	%xmm2, -192(%rbp)
	movaps	%xmm3, -176(%rbp)
	movaps	%xmm4, -160(%rbp)
	movaps	%xmm5, -144(%rbp)
	movaps	%xmm6, -128(%rbp)
	movaps	%xmm7, -112(%rbp)
.LBB18_5:
	movq	%r9, -232(%rbp)
	movq	%r8, -240(%rbp)
	movq	%rcx, -248(%rbp)
	movq	%rdx, -256(%rbp)
	movq	%rsi, -264(%rbp)
	movq	%rdi, -272(%rbp)
	movss	%xmm0, -4(%rbp)
	movabsq	$.L.str.34, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-64(%rbp), %rax
	leaq	-272(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$64, 4(%rax)
	movl	$0, (%rax)
	leaq	-64(%rbp), %rcx
	movl	-64(%rbp), %edx
	cmpl	$32, %edx
	ja	.LBB18_2
# %bb.1:
	movslq	%edx, %rax
	addq	16(%rcx), %rax
	addl	$16, %edx
	movl	%edx, (%rcx)
	jmp	.LBB18_3
.LBB18_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$16, %rdx
	movq	%rdx, 8(%rcx)
.LBB18_3:
	movups	(%rax), %xmm0
	movaps	%xmm0, -32(%rbp)
	xorps	%xmm0, %xmm0
	movl	$1, %edi
	movb	$1, %al
	callq	FF_function_45
	movq	%rax, -96(%rbp)
	movq	%rdx, -88(%rbp)
	xorps	%xmm0, %xmm0
	movl	$1, %edi
	movl	$2, %esi
	movl	$3, %edx
	movb	$1, %al
	callq	FF_function_45
	movq	%rax, -80(%rbp)
	movq	%rdx, -72(%rbp)
	movl	$.L.str.35, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movq	-32(%rbp), %rax
	movq	-24(%rbp), %rdx
	addq	$272, %rsp                      # imm = 0x110
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end18:
	.size	FF_function_45, .Lfunc_end18-FF_function_45
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_49                  # -- Begin function FF_function_49
	.p2align	4, 0x90
	.type	FF_function_49,@function
FF_function_49:                         # @FF_function_49
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$272, %rsp                      # imm = 0x110
	testb	%al, %al
	je	.LBB19_5
# %bb.4:
	movaps	%xmm0, -224(%rbp)
	movaps	%xmm1, -208(%rbp)
	movaps	%xmm2, -192(%rbp)
	movaps	%xmm3, -176(%rbp)
	movaps	%xmm4, -160(%rbp)
	movaps	%xmm5, -144(%rbp)
	movaps	%xmm6, -128(%rbp)
	movaps	%xmm7, -112(%rbp)
.LBB19_5:
	movq	%r9, -232(%rbp)
	movq	%r8, -240(%rbp)
	movq	%rcx, -248(%rbp)
	movq	%rdx, -256(%rbp)
	movq	%rsi, -264(%rbp)
	movq	%rdi, -88(%rbp)
	movabsq	$.L.str.36, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-48(%rbp), %rax
	leaq	-272(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$48, 4(%rax)
	movl	$8, (%rax)
	leaq	-48(%rbp), %rcx
	movl	-48(%rbp), %edx
	cmpl	$32, %edx
	ja	.LBB19_2
# %bb.1:
	movslq	%edx, %rax
	addq	16(%rcx), %rax
	addl	$16, %edx
	movl	%edx, (%rcx)
	jmp	.LBB19_3
.LBB19_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$16, %rdx
	movq	%rdx, 8(%rcx)
.LBB19_3:
	movups	(%rax), %xmm0
	movaps	%xmm0, -16(%rbp)
	xorl	%edi, %edi
	movl	$1, %esi
	xorl	%eax, %eax
	callq	FF_function_49
	movq	%rax, -80(%rbp)
	movq	%rdx, -72(%rbp)
	xorl	%edi, %edi
	movl	$1, %esi
	movl	$2, %edx
	movl	$3, %ecx
	xorl	%eax, %eax
	callq	FF_function_49
	movq	%rax, -64(%rbp)
	movq	%rdx, -56(%rbp)
	movl	$.L.str.37, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movq	-16(%rbp), %rax
	movq	-8(%rbp), %rdx
	addq	$272, %rsp                      # imm = 0x110
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end19:
	.size	FF_function_49, .Lfunc_end19-FF_function_49
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_54                  # -- Begin function FF_function_54
	.p2align	4, 0x90
	.type	FF_function_54,@function
FF_function_54:                         # @FF_function_54
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$256, %rsp                      # imm = 0x100
	testb	%al, %al
	je	.LBB20_5
# %bb.4:
	movaps	%xmm1, -192(%rbp)
	movaps	%xmm2, -176(%rbp)
	movaps	%xmm3, -160(%rbp)
	movaps	%xmm4, -144(%rbp)
	movaps	%xmm5, -128(%rbp)
	movaps	%xmm6, -112(%rbp)
	movaps	%xmm7, -96(%rbp)
.LBB20_5:
	movq	%r9, -216(%rbp)
	movq	%r8, -224(%rbp)
	movq	%rcx, -232(%rbp)
	movq	%rdx, -240(%rbp)
	movq	%rsi, -248(%rbp)
	movq	%rdi, -256(%rbp)
	movsd	%xmm0, -72(%rbp)
	movabsq	$.L.str.38, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-48(%rbp), %rax
	leaq	-256(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$64, 4(%rax)
	movl	$0, (%rax)
	leaq	-48(%rbp), %rcx
	movq	%rcx, %rdx
	addq	$4, %rdx
	movl	-44(%rbp), %esi
	cmpl	$160, %esi
	ja	.LBB20_2
# %bb.1:
	movslq	%esi, %rax
	addq	16(%rcx), %rax
	addl	$16, %esi
	movl	%esi, (%rdx)
	jmp	.LBB20_3
.LBB20_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$8, %rdx
	movq	%rdx, 8(%rcx)
.LBB20_3:
	movq	(%rax), %rax
	movq	%rax, -24(%rbp)
	movq	$0, -16(%rbp)
	movsd	-16(%rbp), %xmm0                # xmm0 = mem[0],zero
	movl	$1, %edi
	movb	$1, %al
	callq	FF_function_54
	movsd	%xmm0, -64(%rbp)
	movq	$0, -8(%rbp)
	movsd	-8(%rbp), %xmm0                 # xmm0 = mem[0],zero
	movl	$1, %edi
	movl	$2, %esi
	movl	$3, %edx
	movb	$1, %al
	callq	FF_function_54
	movsd	%xmm0, -56(%rbp)
	movl	$.L.str.39, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movsd	-24(%rbp), %xmm0                # xmm0 = mem[0],zero
	addq	$256, %rsp                      # imm = 0x100
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end20:
	.size	FF_function_54, .Lfunc_end20-FF_function_54
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_56                  # -- Begin function FF_function_56
	.p2align	4, 0x90
	.type	FF_function_56,@function
FF_function_56:                         # @FF_function_56
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movq	%rdi, -32(%rbp)
	movb	%sil, -1(%rbp)
	movq	%rdx, -24(%rbp)
	movabsq	$.L.str.40, %rdi
	movb	$0, %al
	callq	printf@PLT
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB21_2
# %bb.1:
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	jmp	.LBB21_3
.LBB21_2:
	movw	$0, -8(%rbp)
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
.LBB21_3:
	movsd	-16(%rbp), %xmm0                # xmm0 = mem[0],zero
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end21:
	.size	FF_function_56, .Lfunc_end21-FF_function_56
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_55                  # -- Begin function FF_function_55
	.p2align	4, 0x90
	.type	FF_function_55,@function
FF_function_55:                         # @FF_function_55
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$80, %rsp
	movw	56(%rbp), %ax
	movb	48(%rbp), %al
	movq	40(%rbp), %rax
	movw	32(%rbp), %ax
	movq	24(%rbp), %rax
	movq	16(%rbp), %rax
	movb	%dil, -2(%rbp)
	movb	%sil, -1(%rbp)
	movss	%xmm0, -44(%rbp)
	movsd	%xmm1, -80(%rbp)
	movw	%dx, -28(%rbp)
	movq	%rcx, -72(%rbp)
	movq	%r8, -64(%rbp)
	movw	%r9w, -26(%rbp)
	movsd	%xmm2, -56(%rbp)
	movabsq	$.L.str.41, %rdi
	movb	$0, %al
	callq	printf@PLT
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB22_2
# %bb.1:
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	jmp	.LBB22_11
.LBB22_2:
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB22_4
# %bb.3:
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	jmp	.LBB22_11
.LBB22_4:
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB22_6
# %bb.5:
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	jmp	.LBB22_11
.LBB22_6:
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB22_8
# %bb.7:
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	jmp	.LBB22_11
.LBB22_8:
	movw	$0, -24(%rbp)
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB22_10
# %bb.9:
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	jmp	.LBB22_11
.LBB22_10:
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	callq	FF_function_56
	movsd	%xmm0, -40(%rbp)
	movabsq	$.L.str.42, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-40(%rbp), %rax
	movq	%rax, -16(%rbp)
.LBB22_11:
	movsd	-16(%rbp), %xmm0                # xmm0 = mem[0],zero
	addq	$80, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end22:
	.size	FF_function_55, .Lfunc_end22-FF_function_55
	.cfi_endproc
                                        # -- End function
	.section	.rodata.cst4,"aM",@progbits,4
	.p2align	2                               # -- Begin function FF_function_57
.LCPI23_0:
	.long	0x3f000000                      # float 0.5
	.text
	.globl	FF_function_57
	.p2align	4, 0x90
	.type	FF_function_57,@function
FF_function_57:                         # @FF_function_57
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movb	%dil, -1(%rbp)
	movabsq	$.L.str.43, %rdi
	movb	$0, %al
	callq	printf@PLT
	movl	$0, -24(%rbp)
	movss	.LCPI23_0(%rip), %xmm0          # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -20(%rbp)
	movq	$0, -16(%rbp)
	xorl	%eax, %eax
	movsbl	%al, %eax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end23:
	.size	FF_function_57, .Lfunc_end23-FF_function_57
	.cfi_endproc
                                        # -- End function
	.section	.rodata.cst4,"aM",@progbits,4
	.p2align	2                               # -- Begin function FF_function_58
.LCPI24_0:
	.long	0x3f000000                      # float 0.5
	.text
	.globl	FF_function_58
	.p2align	4, 0x90
	.type	FF_function_58,@function
FF_function_58:                         # @FF_function_58
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movw	%di, -4(%rbp)
	movabsq	$.L.str.44, %rdi
	movb	$0, %al
	callq	printf@PLT
	movl	$0, -24(%rbp)
	movss	.LCPI24_0(%rip), %xmm0          # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -20(%rbp)
	movq	$0, -16(%rbp)
	movb	$0, -1(%rbp)
	movabsq	$.L.str.45, %rdi
	movb	$0, %al
	callq	printf@PLT
	movsbl	-1(%rbp), %eax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end24:
	.size	FF_function_58, .Lfunc_end24-FF_function_58
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_60                  # -- Begin function FF_function_60
	.p2align	4, 0x90
	.type	FF_function_60,@function
FF_function_60:                         # @FF_function_60
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$224, %rsp
	testb	%al, %al
	je	.LBB25_5
# %bb.4:
	movaps	%xmm0, -176(%rbp)
	movaps	%xmm1, -160(%rbp)
	movaps	%xmm2, -144(%rbp)
	movaps	%xmm3, -128(%rbp)
	movaps	%xmm4, -112(%rbp)
	movaps	%xmm5, -96(%rbp)
	movaps	%xmm6, -80(%rbp)
	movaps	%xmm7, -64(%rbp)
.LBB25_5:
	movq	%r9, -184(%rbp)
	movq	%r8, -192(%rbp)
	movq	%rcx, -200(%rbp)
	movq	%rdx, -208(%rbp)
	movq	%rsi, -216(%rbp)
	movq	%rdi, -40(%rbp)
	movabsq	$.L.str.46, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-32(%rbp), %rax
	leaq	-224(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$48, 4(%rax)
	movl	$8, (%rax)
	leaq	-32(%rbp), %rcx
	movq	%rcx, %rdx
	addq	$4, %rdx
	movl	-28(%rbp), %esi
	cmpl	$160, %esi
	ja	.LBB25_2
# %bb.1:
	movslq	%esi, %rax
	addq	16(%rcx), %rax
	addl	$16, %esi
	movl	%esi, (%rdx)
	jmp	.LBB25_3
.LBB25_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$8, %rdx
	movq	%rdx, 8(%rcx)
.LBB25_3:
	movss	(%rax), %xmm0                   # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -4(%rbp)
	xorl	%edi, %edi
	movl	$1, %esi
	xorl	%eax, %eax
	callq	FF_function_60
	xorl	%edi, %edi
	movl	$1, %esi
	movl	$2, %edx
	movl	$3, %ecx
	xorl	%eax, %eax
	callq	FF_function_60
	movl	$.L.str.47, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movss	-4(%rbp), %xmm0                 # xmm0 = mem[0],zero,zero,zero
	addq	$224, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end25:
	.size	FF_function_60, .Lfunc_end25-FF_function_60
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_61                  # -- Begin function FF_function_61
	.p2align	4, 0x90
	.type	FF_function_61,@function
FF_function_61:                         # @FF_function_61
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.48, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	$0, -8(%rbp)
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	movabsq	$.L.str.49, %rdi
	movb	$0, %al
	callq	printf@PLT
	movsd	-16(%rbp), %xmm0                # xmm0 = mem[0],zero
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end26:
	.size	FF_function_61, .Lfunc_end26-FF_function_61
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_62                  # -- Begin function FF_function_62
	.p2align	4, 0x90
	.type	FF_function_62,@function
FF_function_62:                         # @FF_function_62
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.50, %rdi
	movb	$0, %al
	callq	printf@PLT
	movb	$0, -8(%rbp)
	movw	$0, -10(%rbp)
	movabsq	$.L.str.51, %rdi
	movb	$0, %al
	callq	printf@PLT
	movzwl	-10(%rbp), %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end27:
	.size	FF_function_62, .Lfunc_end27-FF_function_62
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_63                  # -- Begin function FF_function_63
	.p2align	4, 0x90
	.type	FF_function_63,@function
FF_function_63:                         # @FF_function_63
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.52, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	$0, -8(%rbp)
	movabsq	$.L.str.53, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-8(%rbp), %rax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end28:
	.size	FF_function_63, .Lfunc_end28-FF_function_63
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_64                  # -- Begin function FF_function_64
	.p2align	4, 0x90
	.type	FF_function_64,@function
FF_function_64:                         # @FF_function_64
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.54, %rdi
	movb	$0, %al
	callq	printf@PLT
	movb	$0, -8(%rbp)
	movq	$0, -16(%rbp)
	movabsq	$.L.str.55, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-16(%rbp), %rax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end29:
	.size	FF_function_64, .Lfunc_end29-FF_function_64
	.cfi_endproc
                                        # -- End function
	.section	.rodata.cst4,"aM",@progbits,4
	.p2align	2                               # -- Begin function FF_function_65
.LCPI30_0:
	.long	0x3f000000                      # float 0.5
	.text
	.globl	FF_function_65
	.p2align	4, 0x90
	.type	FF_function_65,@function
FF_function_65:                         # @FF_function_65
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movabsq	$.L.str.56, %rdi
	movb	$0, %al
	callq	printf@PLT
	movl	$0, -24(%rbp)
	movss	.LCPI30_0(%rip), %xmm0          # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -20(%rbp)
	movq	$0, -16(%rbp)
	movw	$0, -2(%rbp)
	movabsq	$.L.str.57, %rdi
	movb	$0, %al
	callq	printf@PLT
	movswl	-2(%rbp), %eax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end30:
	.size	FF_function_65, .Lfunc_end30-FF_function_65
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_66                  # -- Begin function FF_function_66
	.p2align	4, 0x90
	.type	FF_function_66,@function
FF_function_66:                         # @FF_function_66
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.58, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	$0, -8(%rbp)
	movabsq	$.L.str.59, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-8(%rbp), %rax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end31:
	.size	FF_function_66, .Lfunc_end31-FF_function_66
	.cfi_endproc
                                        # -- End function
	.section	.rodata.cst4,"aM",@progbits,4
	.p2align	2                               # -- Begin function FF_function_67
.LCPI32_0:
	.long	0x3f000000                      # float 0.5
	.text
	.globl	FF_function_67
	.p2align	4, 0x90
	.type	FF_function_67,@function
FF_function_67:                         # @FF_function_67
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movabsq	$.L.str.60, %rdi
	movb	$0, %al
	callq	printf@PLT
	movl	$0, -16(%rbp)
	movss	.LCPI32_0(%rip), %xmm0          # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -12(%rbp)
	movq	$0, -8(%rbp)
	xorl	%eax, %eax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end32:
	.size	FF_function_67, .Lfunc_end32-FF_function_67
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_68                  # -- Begin function FF_function_68
	.p2align	4, 0x90
	.type	FF_function_68,@function
FF_function_68:                         # @FF_function_68
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.61, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	$0, -16(%rbp)
	movw	$0, -2(%rbp)
	movabsq	$.L.str.62, %rdi
	movb	$0, %al
	callq	printf@PLT
	movswl	-2(%rbp), %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end33:
	.size	FF_function_68, .Lfunc_end33-FF_function_68
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_69                  # -- Begin function FF_function_69
	.p2align	4, 0x90
	.type	FF_function_69,@function
FF_function_69:                         # @FF_function_69
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.63, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	$0, -8(%rbp)
	movq	$0, -16(%rbp)
	movabsq	$.L.str.64, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-16(%rbp), %rax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end34:
	.size	FF_function_69, .Lfunc_end34-FF_function_69
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_70                  # -- Begin function FF_function_70
	.p2align	4, 0x90
	.type	FF_function_70,@function
FF_function_70:                         # @FF_function_70
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movabsq	$.L.str.65, %rdi
	movb	$0, %al
	callq	printf@PLT
	movb	$0, -8(%rbp)
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB35_2
# %bb.1:
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	jmp	.LBB35_3
.LBB35_2:
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -32(%rbp)
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -24(%rbp)
	movabsq	$.L.str.66, %rdi
	movb	$0, %al
	callq	printf@PLT
	movsd	-24(%rbp), %xmm0                # xmm0 = mem[0],zero
	movsd	%xmm0, -16(%rbp)
.LBB35_3:
	movsd	-16(%rbp), %xmm0                # xmm0 = mem[0],zero
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end35:
	.size	FF_function_70, .Lfunc_end35-FF_function_70
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_71                  # -- Begin function FF_function_71
	.p2align	4, 0x90
	.type	FF_function_71,@function
FF_function_71:                         # @FF_function_71
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.67, %rdi
	movb	$0, %al
	callq	printf@PLT
	movb	$0, -1(%rbp)
	movabsq	$.L.str.68, %rdi
	movb	$0, %al
	callq	printf@PLT
	movsbl	-1(%rbp), %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end36:
	.size	FF_function_71, .Lfunc_end36-FF_function_71
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_72                  # -- Begin function FF_function_72
	.p2align	4, 0x90
	.type	FF_function_72,@function
FF_function_72:                         # @FF_function_72
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.69, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	$0, -16(%rbp)
	movw	$0, -2(%rbp)
	movabsq	$.L.str.70, %rdi
	movb	$0, %al
	callq	printf@PLT
	movswl	-2(%rbp), %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end37:
	.size	FF_function_72, .Lfunc_end37-FF_function_72
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_74                  # -- Begin function FF_function_74
	.p2align	4, 0x90
	.type	FF_function_74,@function
FF_function_74:                         # @FF_function_74
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.71, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	$0, -8(%rbp)
	xorl	%eax, %eax
	cwtl
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end38:
	.size	FF_function_74, .Lfunc_end38-FF_function_74
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_73                  # -- Begin function FF_function_73
	.p2align	4, 0x90
	.type	FF_function_73,@function
FF_function_73:                         # @FF_function_73
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.72, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	$0, -16(%rbp)
	callq	FF_function_74
	movw	%ax, -2(%rbp)
	movabsq	$.L.str.73, %rdi
	movb	$0, %al
	callq	printf@PLT
	movswl	-2(%rbp), %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end39:
	.size	FF_function_73, .Lfunc_end39-FF_function_73
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_76                  # -- Begin function FF_function_76
	.p2align	4, 0x90
	.type	FF_function_76,@function
FF_function_76:                         # @FF_function_76
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.74, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%eax, %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end40:
	.size	FF_function_76, .Lfunc_end40-FF_function_76
	.cfi_endproc
                                        # -- End function
	.section	.rodata.cst4,"aM",@progbits,4
	.p2align	2                               # -- Begin function FF_function_75
.LCPI41_0:
	.long	0x3f000000                      # float 0.5
	.text
	.globl	FF_function_75
	.p2align	4, 0x90
	.type	FF_function_75,@function
FF_function_75:                         # @FF_function_75
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movabsq	$.L.str.75, %rdi
	movb	$0, %al
	callq	printf@PLT
	movl	$0, -24(%rbp)
	movss	.LCPI41_0(%rip), %xmm0          # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -20(%rbp)
	movq	$0, -16(%rbp)
	callq	FF_function_76
	movq	%rax, -8(%rbp)
	movabsq	$.L.str.76, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-8(%rbp), %rax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end41:
	.size	FF_function_75, .Lfunc_end41-FF_function_75
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_78                  # -- Begin function FF_function_78
	.p2align	4, 0x90
	.type	FF_function_78,@function
FF_function_78:                         # @FF_function_78
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$288, %rsp                      # imm = 0x120
	testb	%al, %al
	je	.LBB42_5
# %bb.4:
	movaps	%xmm0, -240(%rbp)
	movaps	%xmm1, -224(%rbp)
	movaps	%xmm2, -208(%rbp)
	movaps	%xmm3, -192(%rbp)
	movaps	%xmm4, -176(%rbp)
	movaps	%xmm5, -160(%rbp)
	movaps	%xmm6, -144(%rbp)
	movaps	%xmm7, -128(%rbp)
.LBB42_5:
	movq	%r9, -248(%rbp)
	movq	%r8, -256(%rbp)
	movq	%rcx, -264(%rbp)
	movq	%rdx, -272(%rbp)
	movq	%rsi, -280(%rbp)
	movq	%rdi, -104(%rbp)
	movabsq	$.L.str.77, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-64(%rbp), %rax
	leaq	-288(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$48, 4(%rax)
	movl	$8, (%rax)
	leaq	-64(%rbp), %rcx
	movl	-64(%rbp), %edx
	cmpl	$32, %edx
	ja	.LBB42_2
# %bb.1:
	movslq	%edx, %rax
	addq	16(%rcx), %rax
	addl	$16, %edx
	movl	%edx, (%rcx)
	jmp	.LBB42_3
.LBB42_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$16, %rdx
	movq	%rdx, 8(%rcx)
.LBB42_3:
	movups	(%rax), %xmm0
	movaps	%xmm0, -32(%rbp)
	movq	$0, -16(%rbp)
	movq	-16(%rbp), %rdi
	movl	$1, %esi
	xorl	%eax, %eax
	callq	FF_function_78
	movq	%rax, -96(%rbp)
	movq	%rdx, -88(%rbp)
	movq	$0, -8(%rbp)
	movq	-8(%rbp), %rdi
	movl	$1, %esi
	movl	$2, %edx
	movl	$3, %ecx
	xorl	%eax, %eax
	callq	FF_function_78
	movq	%rax, -80(%rbp)
	movq	%rdx, -72(%rbp)
	movl	$.L.str.78, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movq	-32(%rbp), %rax
	movq	-24(%rbp), %rdx
	addq	$288, %rsp                      # imm = 0x120
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end42:
	.size	FF_function_78, .Lfunc_end42-FF_function_78
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_80                  # -- Begin function FF_function_80
	.p2align	4, 0x90
	.type	FF_function_80,@function
FF_function_80:                         # @FF_function_80
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.79, %rdi
	movb	$0, %al
	callq	printf@PLT
	movb	$0, -1(%rbp)
	movabsq	$.L.str.80, %rdi
	movb	$0, %al
	callq	printf@PLT
	movsbl	-1(%rbp), %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end43:
	.size	FF_function_80, .Lfunc_end43-FF_function_80
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_81                  # -- Begin function FF_function_81
	.p2align	4, 0x90
	.type	FF_function_81,@function
FF_function_81:                         # @FF_function_81
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movabsq	$.L.str.81, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	$0, -2(%rbp)
	movabsq	$.L.str.82, %rdi
	movb	$0, %al
	callq	printf@PLT
	movswl	-2(%rbp), %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end44:
	.size	FF_function_81, .Lfunc_end44-FF_function_81
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_82                  # -- Begin function FF_function_82
	.p2align	4, 0x90
	.type	FF_function_82,@function
FF_function_82:                         # @FF_function_82
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movq	%rdi, -24(%rbp)
	movss	%xmm0, -12(%rbp)
	movabsq	$.L.str.83, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-8(%rbp), %rdi
	xorl	%esi, %esi
	movl	$8, %edx
	callq	memset@PLT
	movabsq	$.L.str.84, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-8(%rbp), %rax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end45:
	.size	FF_function_82, .Lfunc_end45-FF_function_82
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_79                  # -- Begin function FF_function_79
	.p2align	4, 0x90
	.type	FF_function_79,@function
FF_function_79:                         # @FF_function_79
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$64, %rsp
	movss	%xmm0, -20(%rbp)
	movq	%rdi, -48(%rbp)
	movw	%si, -2(%rbp)
	movsd	%xmm1, -40(%rbp)
	movq	%rdx, -32(%rbp)
	movabsq	$.L.str.85, %rdi
	movb	$0, %al
	callq	printf@PLT
	callq	FF_function_80
	movq	$0, -16(%rbp)
	movq	-16(%rbp), %rax
	addq	$64, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end46:
	.size	FF_function_79, .Lfunc_end46-FF_function_79
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_83                  # -- Begin function FF_function_83
	.p2align	4, 0x90
	.type	FF_function_83,@function
FF_function_83:                         # @FF_function_83
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movw	%di, -2(%rbp)
	movsd	%xmm0, -24(%rbp)
	movabsq	$.L.str.86, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	$0, -16(%rbp)
	xorps	%xmm0, %xmm0
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end47:
	.size	FF_function_83, .Lfunc_end47-FF_function_83
	.cfi_endproc
                                        # -- End function
	.section	.rodata.cst4,"aM",@progbits,4
	.p2align	2                               # -- Begin function FF_function_84
.LCPI48_0:
	.long	0x3f000000                      # float 0.5
	.text
	.globl	FF_function_84
	.p2align	4, 0x90
	.type	FF_function_84,@function
FF_function_84:                         # @FF_function_84
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movw	%di, -2(%rbp)
	movabsq	$.L.str.87, %rdi
	movb	$0, %al
	callq	printf@PLT
	movl	$0, -32(%rbp)
	movss	.LCPI48_0(%rip), %xmm0          # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -28(%rbp)
	movq	$0, -24(%rbp)
	movq	$0, -16(%rbp)
	movabsq	$.L.str.88, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-16(%rbp), %rax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end48:
	.size	FF_function_84, .Lfunc_end48-FF_function_84
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_86                  # -- Begin function FF_function_86
	.p2align	4, 0x90
	.type	FF_function_86,@function
FF_function_86:                         # @FF_function_86
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$208, %rsp
	testb	%al, %al
	je	.LBB49_5
# %bb.4:
	movaps	%xmm0, -160(%rbp)
	movaps	%xmm1, -144(%rbp)
	movaps	%xmm2, -128(%rbp)
	movaps	%xmm3, -112(%rbp)
	movaps	%xmm4, -96(%rbp)
	movaps	%xmm5, -80(%rbp)
	movaps	%xmm6, -64(%rbp)
	movaps	%xmm7, -48(%rbp)
.LBB49_5:
	movq	%r9, -168(%rbp)
	movq	%r8, -176(%rbp)
	movq	%rcx, -184(%rbp)
	movq	%rdx, -192(%rbp)
	movq	%rsi, -200(%rbp)
	movw	%di, -4(%rbp)
	movabsq	$.L.str.89, %rdi
	movb	$0, %al
	callq	printf@PLT
	leaq	-32(%rbp), %rax
	leaq	-208(%rbp), %rcx
	movq	%rcx, 16(%rax)
	leaq	16(%rbp), %rcx
	movq	%rcx, 8(%rax)
	movl	$48, 4(%rax)
	movl	$8, (%rax)
	leaq	-32(%rbp), %rcx
	movl	-32(%rbp), %edx
	cmpl	$40, %edx
	ja	.LBB49_2
# %bb.1:
	movslq	%edx, %rax
	addq	16(%rcx), %rax
	addl	$8, %edx
	movl	%edx, (%rcx)
	jmp	.LBB49_3
.LBB49_2:
	movq	8(%rcx), %rax
	movq	%rax, %rdx
	addq	$8, %rdx
	movq	%rdx, 8(%rcx)
.LBB49_3:
	movw	(%rax), %ax
	movw	%ax, -2(%rbp)
	xorl	%edi, %edi
	movl	$1, %esi
	xorl	%eax, %eax
	callq	FF_function_86
	xorl	%edi, %edi
	movl	$1, %esi
	movl	$2, %edx
	movl	$3, %ecx
	xorl	%eax, %eax
	callq	FF_function_86
	movl	$.L.str.90, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movw	-2(%rbp), %ax
	addq	$208, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end49:
	.size	FF_function_86, .Lfunc_end49-FF_function_86
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_52                  # -- Begin function FF_function_52
	.p2align	4, 0x90
	.type	FF_function_52,@function
FF_function_52:                         # @FF_function_52
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	pushq	%r15
	pushq	%r14
	pushq	%r13
	pushq	%r12
	pushq	%rbx
	subq	$168, %rsp
	.cfi_offset %rbx, -56
	.cfi_offset %r12, -48
	.cfi_offset %r13, -40
	.cfi_offset %r14, -32
	.cfi_offset %r15, -24
	movq	%rdi, -152(%rbp)
	movw	%si, -62(%rbp)
	movw	%dx, -60(%rbp)
	movw	%cx, -58(%rbp)
	movsd	%xmm0, -144(%rbp)
	movl	$.L.str.91, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	xorl	%edi, %edi
	callq	FF_function_57
	movb	%al, -42(%rbp)                  # 1-byte Spill
	xorl	%edi, %edi
	callq	FF_function_58
	movb	%al, -41(%rbp)                  # 1-byte Spill
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	xorl	%eax, %eax
	callq	FF_function_60
	movss	%xmm0, -56(%rbp)                # 4-byte Spill
	callq	FF_function_61
	movsd	%xmm0, -96(%rbp)                # 8-byte Spill
	callq	FF_function_62
	movw	%ax, -46(%rbp)                  # 2-byte Spill
	callq	FF_function_63
	movq	%rax, -88(%rbp)                 # 8-byte Spill
	callq	FF_function_64
	movq	%rax, -80(%rbp)                 # 8-byte Spill
	callq	FF_function_65
	movw	%ax, -44(%rbp)                  # 2-byte Spill
	callq	FF_function_66
	movq	%rax, %r15
	callq	FF_function_67
	movq	%rax, %r12
	callq	FF_function_68
	movw	%ax, %bx
	callq	FF_function_69
	movq	%rax, %r13
	callq	FF_function_70
	movsd	%xmm0, -72(%rbp)                # 8-byte Spill
	callq	FF_function_71
	movb	%al, %r14b
	callq	FF_function_72
	cwtl
	movl	%eax, 40(%rsp)
	movsbl	%r14b, %eax
	movl	%eax, 32(%rsp)
	movq	%r13, 24(%rsp)
	movswl	%bx, %eax
	movl	%eax, 16(%rsp)
	movq	%r12, 8(%rsp)
	movq	%r15, (%rsp)
	movsbl	-42(%rbp), %edi                 # 1-byte Folded Reload
	movsbl	-41(%rbp), %esi                 # 1-byte Folded Reload
	movzwl	-46(%rbp), %edx                 # 2-byte Folded Reload
	movswl	-44(%rbp), %r9d                 # 2-byte Folded Reload
	movss	-56(%rbp), %xmm0                # 4-byte Reload
                                        # xmm0 = mem[0],zero,zero,zero
	movsd	-96(%rbp), %xmm1                # 8-byte Reload
                                        # xmm1 = mem[0],zero
	movq	-88(%rbp), %rcx                 # 8-byte Reload
	movq	-80(%rbp), %r8                  # 8-byte Reload
	movsd	-72(%rbp), %xmm2                # 8-byte Reload
                                        # xmm2 = mem[0],zero
	callq	FF_function_55
	movsd	%xmm0, -112(%rbp)
	callq	FF_function_73
	movswl	%ax, %ebx
	callq	FF_function_75
	movsd	-112(%rbp), %xmm0               # xmm0 = mem[0],zero
	movl	%ebx, %edi
	movq	%rax, %rsi
	movb	$1, %al
	callq	FF_function_54
	movsd	%xmm0, -136(%rbp)
	xorl	%edi, %edi
	xorps	%xmm0, %xmm0
	callq	FF_function_83
	movss	%xmm0, -52(%rbp)                # 4-byte Spill
	xorl	%edi, %edi
	callq	FF_function_84
	movq	%rax, %rbx
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorps	%xmm0, %xmm0
	movb	$1, %al
	callq	FF_function_86
	movswl	%ax, %esi
	movss	-52(%rbp), %xmm0                # 4-byte Reload
                                        # xmm0 = mem[0],zero,zero,zero
	movq	%rbx, %rdi
	xorps	%xmm1, %xmm1
	xorl	%edx, %edx
	callq	FF_function_79
	movq	%rax, -104(%rbp)
	movq	-104(%rbp), %rdi
	xorl	%esi, %esi
	xorps	%xmm0, %xmm0
	movb	$1, %al
	callq	FF_function_78
	movq	%rax, -128(%rbp)
	movq	%rdx, -120(%rbp)
	movl	$.L.str.92, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movq	-128(%rbp), %rax
	movq	-120(%rbp), %rdx
	addq	$168, %rsp
	popq	%rbx
	popq	%r12
	popq	%r13
	popq	%r14
	popq	%r15
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end50:
	.size	FF_function_52, .Lfunc_end50-FF_function_52
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_51                  # -- Begin function FF_function_51
	.p2align	4, 0x90
	.type	FF_function_51,@function
FF_function_51:                         # @FF_function_51
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movb	%dil, -1(%rbp)
	movl	$.L.str.93, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	xorl	%ecx, %ecx
	xorps	%xmm0, %xmm0
	callq	FF_function_52
	movq	%rax, -48(%rbp)
	movq	%rdx, -40(%rbp)
	movups	.L__const.FF_function_51.FF_x(%rip), %xmm0
	movaps	%xmm0, -32(%rbp)
	movl	$.L.str.94, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movq	-32(%rbp), %rax
	movq	-24(%rbp), %rdx
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end51:
	.size	FF_function_51, .Lfunc_end51-FF_function_51
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_50                  # -- Begin function FF_function_50
	.p2align	4, 0x90
	.type	FF_function_50,@function
FF_function_50:                         # @FF_function_50
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movss	%xmm0, -12(%rbp)
	movabsq	$.L.str.95, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	callq	FF_function_51
	movq	%rax, -32(%rbp)
	movq	%rdx, -24(%rbp)
	movq	$0, -8(%rbp)
	movabsq	$.L.str.96, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-8(%rbp), %rax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end52:
	.size	FF_function_50, .Lfunc_end52-FF_function_50
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_47                  # -- Begin function FF_function_47
	.p2align	4, 0x90
	.type	FF_function_47,@function
FF_function_47:                         # @FF_function_47
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movq	%rdi, -40(%rbp)
	movq	%rsi, -32(%rbp)
	movabsq	$.L.str.97, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorps	%xmm0, %xmm0
	callq	FF_function_50
	movq	%rax, %rdi
	xorl	%esi, %esi
	xorl	%edx, %edx
	movb	$0, %al
	callq	FF_function_49
	movq	%rax, -24(%rbp)
	movq	%rdx, -16(%rbp)
	leaq	-8(%rbp), %rdi
	xorl	%esi, %esi
	movl	$2, %edx
	callq	memset@PLT
	movabsq	$.L.str.98, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	-8(%rbp), %ax
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end53:
	.size	FF_function_47, .Lfunc_end53-FF_function_47
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_46                  # -- Begin function FF_function_46
	.p2align	4, 0x90
	.type	FF_function_46,@function
FF_function_46:                         # @FF_function_46
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movq	%rdi, -16(%rbp)
	movb	%sil, -1(%rbp)
	movabsq	$.L.str.99, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	callq	FF_function_47
	movw	%ax, -8(%rbp)
	xorps	%xmm0, %xmm0
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end54:
	.size	FF_function_46, .Lfunc_end54-FF_function_46
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_43                  # -- Begin function FF_function_43
	.p2align	4, 0x90
	.type	FF_function_43,@function
FF_function_43:                         # @FF_function_43
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movss	%xmm0, -12(%rbp)
	movw	%di, -4(%rbp)
	movw	%si, -2(%rbp)
	movss	%xmm1, -8(%rbp)
	movabsq	$.L.str.100, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	callq	FF_function_46
	xorl	%edi, %edi
	xorl	%esi, %esi
	movb	$1, %al
	callq	FF_function_45
	movq	%rax, -32(%rbp)
	movq	%rdx, -24(%rbp)
	xorl	%eax, %eax
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end55:
	.size	FF_function_43, .Lfunc_end55-FF_function_43
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_40                  # -- Begin function FF_function_40
	.p2align	4, 0x90
	.type	FF_function_40,@function
FF_function_40:                         # @FF_function_40
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movq	%rdi, -40(%rbp)
	movq	%rsi, -32(%rbp)
	movq	%rdx, -24(%rbp)
	movl	$.L.str.101, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	xorps	%xmm0, %xmm0
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorps	%xmm1, %xmm1
	callq	FF_function_43
	movq	%rax, %rdi
	xorl	%esi, %esi
	xorl	%edx, %edx
	xorl	%eax, %eax
	callq	FF_function_42
	movups	.L__const.FF_function_40.FF_x(%rip), %xmm0
	movaps	%xmm0, -16(%rbp)
	movl	$.L.str.102, %edi
	xorl	%eax, %eax
	callq	printf@PLT
	movq	-16(%rbp), %rax
	movq	-8(%rbp), %rdx
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end56:
	.size	FF_function_40, .Lfunc_end56-FF_function_40
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_39                  # -- Begin function FF_function_39
	.p2align	4, 0x90
	.type	FF_function_39,@function
FF_function_39:                         # @FF_function_39
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movb	%dil, -9(%rbp)
	movq	%rsi, -48(%rbp)
	movq	%rdx, -40(%rbp)
	movabsq	$.L.str.103, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	callq	FF_function_40
	movq	%rax, -32(%rbp)
	movq	%rdx, -24(%rbp)
	leaq	-8(%rbp), %rdi
	xorl	%esi, %esi
	movl	$2, %edx
	callq	memset@PLT
	movabsq	$.L.str.104, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	-8(%rbp), %ax
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end57:
	.size	FF_function_39, .Lfunc_end57-FF_function_39
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_38                  # -- Begin function FF_function_38
	.p2align	4, 0x90
	.type	FF_function_38,@function
FF_function_38:                         # @FF_function_38
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movq	%rdi, -40(%rbp)
	movw	%si, -10(%rbp)
	movsd	%xmm0, -32(%rbp)
	movabsq	$.L.str.105, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	callq	FF_function_39
	movw	%ax, -8(%rbp)
	movq	$0, -24(%rbp)
	movabsq	$.L.str.106, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-24(%rbp), %rax
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end58:
	.size	FF_function_38, .Lfunc_end58-FF_function_38
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_37                  # -- Begin function FF_function_37
	.p2align	4, 0x90
	.type	FF_function_37,@function
FF_function_37:                         # @FF_function_37
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movb	%dil, -2(%rbp)
	movq	%rsi, -16(%rbp)
	movabsq	$.L.str.107, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorps	%xmm0, %xmm0
	callq	FF_function_38
	movb	$0, -1(%rbp)
	movabsq	$.L.str.108, %rdi
	movb	$0, %al
	callq	printf@PLT
	movzbl	-1(%rbp), %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end59:
	.size	FF_function_37, .Lfunc_end59-FF_function_37
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_34                  # -- Begin function FF_function_34
	.p2align	4, 0x90
	.type	FF_function_34,@function
FF_function_34:                         # @FF_function_34
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movq	%rdi, -40(%rbp)
	movb	%sil, -1(%rbp)
	movw	%dx, -10(%rbp)
	movq	%rcx, -32(%rbp)
	movabsq	$.L.str.109, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	callq	FF_function_37
	movzbl	%al, %edi
	xorps	%xmm0, %xmm0
	xorl	%esi, %esi
	movb	$1, %al
	callq	FF_function_36
	movw	%ax, -8(%rbp)
	movq	$0, -24(%rbp)
	movabsq	$.L.str.110, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-24(%rbp), %rax
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end60:
	.size	FF_function_34, .Lfunc_end60-FF_function_34
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_33                  # -- Begin function FF_function_33
	.p2align	4, 0x90
	.type	FF_function_33,@function
FF_function_33:                         # @FF_function_33
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movw	%di, -2(%rbp)
	movabsq	$.L.str.111, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	xorl	%ecx, %ecx
	callq	FF_function_34
	movq	$0, -16(%rbp)
	movabsq	$.L.str.112, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-16(%rbp), %rax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end61:
	.size	FF_function_33, .Lfunc_end61-FF_function_33
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_32                  # -- Begin function FF_function_32
	.p2align	4, 0x90
	.type	FF_function_32,@function
FF_function_32:                         # @FF_function_32
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movq	%rdi, -32(%rbp)
	movsd	%xmm0, -24(%rbp)
	movsd	%xmm1, -16(%rbp)
	movabsq	$.L.str.113, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	callq	FF_function_33
	leaq	-8(%rbp), %rdi
	xorl	%esi, %esi
	movl	$2, %edx
	callq	memset@PLT
	movabsq	$.L.str.114, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	-8(%rbp), %ax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end62:
	.size	FF_function_32, .Lfunc_end62-FF_function_32
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_31                  # -- Begin function FF_function_31
	.p2align	4, 0x90
	.type	FF_function_31,@function
FF_function_31:                         # @FF_function_31
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movsd	%xmm0, -16(%rbp)
	movabsq	$.L.str.115, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorps	%xmm0, %xmm0
	xorps	%xmm1, %xmm1
	callq	FF_function_32
	movw	%ax, -8(%rbp)
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB63_2
# %bb.1:
	movb	$0, -1(%rbp)
	jmp	.LBB63_3
.LBB63_2:
	movb	$0, -2(%rbp)
	movabsq	$.L.str.116, %rdi
	movb	$0, %al
	callq	printf@PLT
	movb	-2(%rbp), %al
	movb	%al, -1(%rbp)
.LBB63_3:
	movzbl	-1(%rbp), %eax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end63:
	.size	FF_function_31, .Lfunc_end63-FF_function_31
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_30                  # -- Begin function FF_function_30
	.p2align	4, 0x90
	.type	FF_function_30,@function
FF_function_30:                         # @FF_function_30
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$64, %rsp
	movq	16(%rbp), %rax
	movq	%rdi, -56(%rbp)
	movq	%rsi, -48(%rbp)
	movq	%rdx, -40(%rbp)
	movq	%rcx, -32(%rbp)
	movw	%r8w, -12(%rbp)
	movw	%r9w, -10(%rbp)
	movsd	%xmm0, -24(%rbp)
	movabsq	$.L.str.117, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorps	%xmm0, %xmm0
	callq	FF_function_31
	leaq	-8(%rbp), %rdi
	xorl	%esi, %esi
	movl	$2, %edx
	callq	memset@PLT
	movabsq	$.L.str.118, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	-8(%rbp), %ax
	addq	$64, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end64:
	.size	FF_function_30, .Lfunc_end64-FF_function_30
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_29                  # -- Begin function FF_function_29
	.p2align	4, 0x90
	.type	FF_function_29,@function
FF_function_29:                         # @FF_function_29
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movb	%dil, -1(%rbp)
	movabsq	$.L.str.119, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	xorl	%ecx, %ecx
	xorl	%r8d, %r8d
	xorl	%r9d, %r9d
	xorps	%xmm0, %xmm0
	movq	$0, (%rsp)
	callq	FF_function_30
	movw	%ax, -8(%rbp)
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -16(%rbp)
	movabsq	$.L.str.120, %rdi
	movb	$0, %al
	callq	printf@PLT
	movsd	-16(%rbp), %xmm0                # xmm0 = mem[0],zero
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end65:
	.size	FF_function_29, .Lfunc_end65-FF_function_29
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_28                  # -- Begin function FF_function_28
	.p2align	4, 0x90
	.type	FF_function_28,@function
FF_function_28:                         # @FF_function_28
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movw	%di, -14(%rbp)
	movb	%sil, -9(%rbp)
	movw	%dx, -12(%rbp)
	movabsq	$.L.str.121, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	callq	FF_function_29
	movw	$0, -8(%rbp)
	movw	-8(%rbp), %ax
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end66:
	.size	FF_function_28, .Lfunc_end66-FF_function_28
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_27                  # -- Begin function FF_function_27
	.p2align	4, 0x90
	.type	FF_function_27,@function
FF_function_27:                         # @FF_function_27
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movss	%xmm0, -24(%rbp)
	movb	%dil, -1(%rbp)
	movss	%xmm1, -20(%rbp)
	movabsq	$.L.str.122, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	callq	FF_function_28
	movw	%ax, -16(%rbp)
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB67_2
# %bb.1:
	xorps	%xmm0, %xmm0
	movss	%xmm0, -8(%rbp)
	jmp	.LBB67_3
.LBB67_2:
	xorps	%xmm0, %xmm0
	movss	%xmm0, -8(%rbp)
.LBB67_3:
	movss	-8(%rbp), %xmm0                 # xmm0 = mem[0],zero,zero,zero
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end67:
	.size	FF_function_27, .Lfunc_end67-FF_function_27
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_26                  # -- Begin function FF_function_26
	.p2align	4, 0x90
	.type	FF_function_26,@function
FF_function_26:                         # @FF_function_26
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movb	%dil, -9(%rbp)
	movq	%rsi, -32(%rbp)
	movq	%rdx, -24(%rbp)
	movw	%cx, -16(%rbp)
	movw	%r8w, -14(%rbp)
	movw	%r9w, -12(%rbp)
	movabsq	$.L.str.123, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorps	%xmm0, %xmm0
	xorl	%edi, %edi
	xorps	%xmm1, %xmm1
	callq	FF_function_27
	leaq	-8(%rbp), %rdi
	xorl	%esi, %esi
	movl	$2, %edx
	callq	memset@PLT
	movabsq	$.L.str.124, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	-8(%rbp), %ax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end68:
	.size	FF_function_26, .Lfunc_end68-FF_function_26
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_21                  # -- Begin function FF_function_21
	.p2align	4, 0x90
	.type	FF_function_21,@function
FF_function_21:                         # @FF_function_21
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movq	%rdi, -48(%rbp)
	movq	%rsi, -40(%rbp)
	movw	%dx, -10(%rbp)
	movsd	%xmm0, -32(%rbp)
	movabsq	$.L.str.125, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	xorl	%ecx, %ecx
	xorl	%r8d, %r8d
	xorl	%r9d, %r9d
	callq	FF_function_26
	movw	%ax, -8(%rbp)
	movzwl	-8(%rbp), %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	movb	$0, %al
	callq	FF_function_25
	xorl	%edi, %edi
	xorl	%esi, %esi
	movb	$1, %al
	callq	FF_function_23
	movq	$0, -24(%rbp)
	movabsq	$.L.str.126, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-24(%rbp), %rax
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end69:
	.size	FF_function_21, .Lfunc_end69-FF_function_21
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_20                  # -- Begin function FF_function_20
	.p2align	4, 0x90
	.type	FF_function_20,@function
FF_function_20:                         # @FF_function_20
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movb	%dil, -9(%rbp)
	movq	%rsi, -32(%rbp)
	movss	%xmm0, -16(%rbp)
	movsd	%xmm1, -24(%rbp)
	movabsq	$.L.str.127, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	xorps	%xmm0, %xmm0
	callq	FF_function_21
	leaq	-8(%rbp), %rdi
	xorl	%esi, %esi
	movl	$2, %edx
	callq	memset@PLT
	movabsq	$.L.str.128, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	-8(%rbp), %ax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end70:
	.size	FF_function_20, .Lfunc_end70-FF_function_20
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_19                  # -- Begin function FF_function_19
	.p2align	4, 0x90
	.type	FF_function_19,@function
FF_function_19:                         # @FF_function_19
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movq	%rdi, -24(%rbp)
	movabsq	$.L.str.129, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorps	%xmm0, %xmm0
	xorps	%xmm1, %xmm1
	callq	FF_function_20
	movw	%ax, -16(%rbp)
	leaq	-8(%rbp), %rdi
	xorl	%esi, %esi
	movl	$2, %edx
	callq	memset@PLT
	movabsq	$.L.str.130, %rdi
	movb	$0, %al
	callq	printf@PLT
	movw	-8(%rbp), %ax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end71:
	.size	FF_function_19, .Lfunc_end71-FF_function_19
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_18                  # -- Begin function FF_function_18
	.p2align	4, 0x90
	.type	FF_function_18,@function
FF_function_18:                         # @FF_function_18
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$48, %rsp
	movq	16(%rbp), %rax
	movq	%rdi, -48(%rbp)
	movw	%si, -12(%rbp)
	movq	%rdx, -40(%rbp)
	movq	%rcx, -32(%rbp)
	movq	%r8, -24(%rbp)
	movw	%r9w, -10(%rbp)
	movabsq	$.L.str.131, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	callq	FF_function_19
	movw	%ax, -8(%rbp)
	movb	$0, -1(%rbp)
	movabsq	$.L.str.132, %rdi
	movb	$0, %al
	callq	printf@PLT
	movzbl	-1(%rbp), %eax
	addq	$48, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end72:
	.size	FF_function_18, .Lfunc_end72-FF_function_18
	.cfi_endproc
                                        # -- End function
	.globl	FF_function_17                  # -- Begin function FF_function_17
	.p2align	4, 0x90
	.type	FF_function_17,@function
FF_function_17:                         # @FF_function_17
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movb	%dil, -3(%rbp)
	movq	%rsi, -16(%rbp)
	movabsq	$.L.str.133, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	xorl	%edx, %edx
	xorl	%ecx, %ecx
	xorl	%r8d, %r8d
	xorl	%r9d, %r9d
	movq	$0, (%rsp)
	callq	FF_function_18
	movw	$0, -2(%rbp)
	movabsq	$.L.str.134, %rdi
	movb	$0, %al
	callq	printf@PLT
	movzwl	-2(%rbp), %eax
	addq	$32, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end73:
	.size	FF_function_17, .Lfunc_end73-FF_function_17
	.cfi_endproc
                                        # -- End function
	.section	.rodata.cst4,"aM",@progbits,4
	.p2align	2                               # -- Begin function FF_function_16
.LCPI74_0:
	.long	0x3f000000                      # float 0.5
	.text
	.globl	FF_function_16
	.p2align	4, 0x90
	.type	FF_function_16,@function
FF_function_16:                         # @FF_function_16
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$64, %rsp
	movss	%xmm0, -36(%rbp)
	movq	%rdi, -56(%rbp)
	movabsq	$.L.str.135, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-48(%rbp), %rdi
	callq	functie_voor_datastructuren
	callq	rand@PLT
	cmpl	$1, %eax
	jge	.LBB74_2
# %bb.1:
	movl	$0, -16(%rbp)
	movss	.LCPI74_0(%rip), %xmm0          # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -12(%rbp)
	movq	$0, -8(%rbp)
	jmp	.LBB74_3
.LBB74_2:
	xorl	%edi, %edi
	xorl	%esi, %esi
	callq	FF_function_17
	movq	.L__const.FF_function_16.FF_x, %rax
	movq	%rax, -32(%rbp)
	movq	.L__const.FF_function_16.FF_x+8, %rax
	movq	%rax, -24(%rbp)
	movabsq	$.L.str.136, %rdi
	movb	$0, %al
	callq	printf@PLT
	movq	-32(%rbp), %rax
	movq	%rax, -16(%rbp)
	movq	-24(%rbp), %rax
	movq	%rax, -8(%rbp)
.LBB74_3:
	movq	-16(%rbp), %rax
	movq	-8(%rbp), %rdx
	addq	$64, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end74:
	.size	FF_function_16, .Lfunc_end74-FF_function_16
	.cfi_endproc
                                        # -- End function
	.globl	CF_function_87                  # -- Begin function CF_function_87
	.p2align	4, 0x90
	.type	CF_function_87,@function
CF_function_87:                         # @CF_function_87
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	subq	$16, %rsp
	movq	%rdi, -16(%rbp)
	movabsq	$.L.str.137, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.138, %rdi
	movb	$0, %al
	callq	printf@PLT
.LBB75_1:                               # =>This Inner Loop Header: Depth=1
	movabsq	$.L.str.139, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	callq	FF_function_57
	callq	getchar@PLT
	cmpl	$23, %eax
	jne	.LBB75_3
# %bb.2:
	jmp	.LBB75_15
.LBB75_3:                               #   in Loop: Header=BB75_1 Depth=1
	movabsq	$.L.str.140, %rdi
	movb	$0, %al
	callq	printf@PLT
	callq	getchar@PLT
	cmpl	$79, %eax
	jne	.LBB75_9
# %bb.4:
	movabsq	$.L.str.141, %rdi
	movb	$0, %al
	callq	printf@PLT
.LBB75_5:                               # =>This Inner Loop Header: Depth=1
	movabsq	$.L.str.142, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.143, %rdi
	movb	$0, %al
	callq	printf@PLT
	callq	getchar@PLT
	cmpl	$31, %eax
	jne	.LBB75_7
# %bb.6:
	jmp	.LBB75_18
.LBB75_7:                               #   in Loop: Header=BB75_5 Depth=1
	callq	FF_function_70
	movabsq	$.L.str.144, %rdi
	movb	$0, %al
	callq	printf@PLT
# %bb.8:                                #   in Loop: Header=BB75_5 Depth=1
	jmp	.LBB75_5
.LBB75_9:                               #   in Loop: Header=BB75_1 Depth=1
	jmp	.LBB75_10
.LBB75_10:                              #   in Loop: Header=BB75_1 Depth=1
	movabsq	$.L.str.145, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.146, %rdi
	movb	$0, %al
	callq	printf@PLT
# %bb.11:                               #   in Loop: Header=BB75_1 Depth=1
	movabsq	$.L.str.147, %rdi
	movb	$0, %al
	callq	printf@PLT
# %bb.12:                               #   in Loop: Header=BB75_1 Depth=1
	jmp	.LBB75_13
.LBB75_13:                              #   in Loop: Header=BB75_1 Depth=1
	movb	$1, %al
	testb	$1, %al
	jne	.LBB75_1
	jmp	.LBB75_14
.LBB75_14:                              # %.loopexit
	jmp	.LBB75_15
.LBB75_15:
	jmp	.LBB75_16
.LBB75_16:
	movabsq	$.L.str.148, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.149, %rdi
	movb	$0, %al
	callq	printf@PLT
# %bb.17:
	movabsq	$.L.str.150, %rdi
	movb	$0, %al
	callq	printf@PLT
	movb	$0, -8(%rbp)
.LBB75_18:
	movb	-8(%rbp), %al
	addq	$16, %rsp
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end75:
	.size	CF_function_87, .Lfunc_end75-CF_function_87
	.cfi_endproc
                                        # -- End function
	.section	.rodata.cst4,"aM",@progbits,4
	.p2align	2                               # -- Begin function main
.LCPI76_0:
	.long	0x3f000000                      # float 0.5
	.text
	.globl	main
	.p2align	4, 0x90
	.type	main,@function
main:                                   # @main
	.cfi_startproc
# %bb.0:
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register %rbp
	pushq	%r15
	pushq	%r14
	pushq	%rbx
	subq	$136, %rsp
	.cfi_offset %rbx, -40
	.cfi_offset %r14, -32
	.cfi_offset %r15, -24
	movl	$0, -36(%rbp)
	movabsq	$.L.str.151, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.152, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorps	%xmm0, %xmm0
	xorl	%edi, %edi
	callq	FF_function_16
	movq	%rax, -56(%rbp)
	movq	%rdx, -48(%rbp)
	movq	-56(%rbp), %rdi
	movq	-48(%rbp), %rsi
	xorl	%edx, %edx
	xorps	%xmm0, %xmm0
	movb	$1, %al
	callq	FF_function_15
	movq	%rax, -72(%rbp)
	movq	%rdx, -64(%rbp)
	movq	-72(%rbp), %rdi
	movq	-64(%rbp), %rsi
	xorps	%xmm0, %xmm0
	xorl	%edx, %edx
	movb	$1, %al
	callq	FF_function_13
	xorps	%xmm1, %xmm1
	xorl	%edi, %edi
	movb	$2, %al
	callq	FF_function_11
	movabsq	$.L.str.153, %rdi
	movb	$0, %al
	callq	printf@PLT
	callq	FF_function_69
	xorps	%xmm0, %xmm0
	movsd	%xmm0, -112(%rbp)
	xorl	%edi, %edi
	xorl	%esi, %esi
	callq	FF_function_37
	movabsq	$.L.str.154, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.155, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	callq	FF_function_57
	movb	%al, %bl
	callq	FF_function_66
	movq	%rax, %r14
	callq	FF_function_73
	movw	%ax, %r15w
	callq	FF_function_73
	xorl	%edi, %edi
	movsbl	%bl, %esi
	xorps	%xmm0, %xmm0
	xorps	%xmm1, %xmm1
	xorl	%edx, %edx
	xorl	%ecx, %ecx
	xorl	%r8d, %r8d
	xorl	%r9d, %r9d
	movq	%r14, (%rsp)
	movq	$0, 8(%rsp)
	movswl	%r15w, %ebx
	movl	%ebx, 16(%rsp)
	movq	$0, 24(%rsp)
	xorps	%xmm2, %xmm2
	movl	$0, 32(%rsp)
	cwtl
	movl	%eax, 40(%rsp)
	callq	FF_function_55
	movsd	%xmm0, -104(%rbp)
	movabsq	$.L.str.156, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.157, %rdi
	movb	$0, %al
	callq	printf@PLT
	movl	$0, -88(%rbp)
	movss	.LCPI76_0(%rip), %xmm0          # xmm0 = mem[0],zero,zero,zero
	movss	%xmm0, -84(%rbp)
	movq	$0, -80(%rbp)
	movabsq	$.L.str.158, %rdi
	movb	$0, %al
	callq	printf@PLT
	movl	$0, -28(%rbp)
.LBB76_1:                               # =>This Inner Loop Header: Depth=1
	movl	-28(%rbp), %esi
	movabsq	$.L.str.159, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.160, %rdi
	movb	$0, %al
	callq	printf@PLT
	callq	getchar@PLT
	cmpl	$97, %eax
	jne	.LBB76_3
# %bb.2:
	movl	$1923, %edi                     # imm = 0x783
	callq	exit@PLT
.LBB76_3:                               #   in Loop: Header=BB76_1 Depth=1
	callq	getchar@PLT
	cmpl	$31, %eax
	jne	.LBB76_5
# %bb.4:
	movl	$0, -36(%rbp)
	jmp	.LBB76_15
.LBB76_5:                               #   in Loop: Header=BB76_1 Depth=1
	callq	getchar@PLT
	cmpl	$19, %eax
	jne	.LBB76_7
# %bb.6:
	jmp	.LBB76_13
.LBB76_7:                               #   in Loop: Header=BB76_1 Depth=1
	movabsq	$.L.str.161, %rdi
	movb	$0, %al
	callq	printf@PLT
	callq	getchar@PLT
	cmpl	$67, %eax
	jne	.LBB76_9
# %bb.8:                                #   in Loop: Header=BB76_1 Depth=1
	jmp	.LBB76_11
.LBB76_9:                               #   in Loop: Header=BB76_1 Depth=1
	xorl	%edi, %edi
	callq	FF_function_29
	xorl	%edi, %edi
	callq	FF_function_83
# %bb.10:                               #   in Loop: Header=BB76_1 Depth=1
	movl	-28(%rbp), %eax
	addl	$13, %eax
	movl	%eax, -28(%rbp)
.LBB76_11:                              #   in Loop: Header=BB76_1 Depth=1
	cmpl	$2494, -28(%rbp)                # imm = 0x9BE
	jne	.LBB76_1
# %bb.12:
	jmp	.LBB76_13
.LBB76_13:
	movabsq	$.L.str.162, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.163, %rdi
	movb	$0, %al
	callq	printf@PLT
# %bb.14:
	xorl	%edi, %edi
	xorl	%esi, %esi
	callq	FF_function_46
	xorl	%edi, %edi
	callq	FF_function_84
	movabsq	$.L.str.164, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.165, %rdi
	movb	$0, %al
	callq	printf@PLT
	movabsq	$.L.str.166, %rdi
	movb	$0, %al
	callq	printf@PLT
	xorl	%edi, %edi
	xorl	%esi, %esi
	callq	FF_function_17
	callq	FF_function_76
	movq	$0, -96(%rbp)
	xorl	%edi, %edi
	callq	FF_function_33
	xorl	%edi, %edi
	callq	CF_function_87
	movb	%al, -32(%rbp)
	movabsq	$.L.str.167, %rdi
	movb	$0, %al
	callq	printf@PLT
	movl	$0, -36(%rbp)
.LBB76_15:
	movl	-36(%rbp), %eax
	addq	$136, %rsp
	popq	%rbx
	popq	%r14
	popq	%r15
	popq	%rbp
	.cfi_def_cfa %rsp, 8
	retq
.Lfunc_end76:
	.size	main, .Lfunc_end76-main
	.cfi_endproc
                                        # -- End function
	.type	.L.str,@object                  # @.str
	.section	.rodata.str1.1,"aMS",@progbits,1
.L.str:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:functie_voor_datastructuren,AUTOGENERATED:T,ID:1c,CHECKSUM:9790"
	.size	.L.str, 128

	.type	.L.str.1,@object                # @.str.1
.L.str.1:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:functie_voor_datastructuren,AUTOGENERATED:T,ID:1d,CHECKSUM:89A5"
	.size	.L.str.1, 117

	.type	.L.str.2,@object                # @.str.2
.L.str.2:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_0,AUTOGENERATED:T,ID:1e,CHECKSUM:D314"
	.size	.L.str.2, 114

	.type	.L.str.1.3,@object              # @.str.1.3
.L.str.1.3:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_0,AUTOGENERATED:T,ID:1f,CHECKSUM:472C"
	.size	.L.str.1.3, 103

	.type	.L.str.2.4,@object              # @.str.2.4
.L.str.2.4:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_1,AUTOGENERATED:T,ID:20,CHECKSUM:8019"
	.size	.L.str.2.4, 114

	.type	.L.str.3,@object                # @.str.3
.L.str.3:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_1,AUTOGENERATED:T,ID:21,CHECKSUM:D5A0"
	.size	.L.str.3, 103

	.type	.L.str.4,@object                # @.str.4
.L.str.4:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_2,AUTOGENERATED:T,ID:22,CHECKSUM:A4CC"
	.size	.L.str.4, 114

	.type	.L.str.5,@object                # @.str.5
.L.str.5:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_2,AUTOGENERATED:T,ID:23,CHECKSUM:F175"
	.size	.L.str.5, 103

	.type	.L.str.6,@object                # @.str.6
.L.str.6:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_3,AUTOGENERATED:T,ID:24,CHECKSUM:3A81"
	.size	.L.str.6, 114

	.type	.L.str.7,@object                # @.str.7
.L.str.7:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_3,AUTOGENERATED:T,ID:25,CHECKSUM:6F38"
	.size	.L.str.7, 103

	.type	.L.str.8,@object                # @.str.8
.L.str.8:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_4,AUTOGENERATED:T,ID:26,CHECKSUM:ED66"
	.size	.L.str.8, 114

	.type	.L.str.9,@object                # @.str.9
.L.str.9:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_4,AUTOGENERATED:T,ID:27,CHECKSUM:B8DF"
	.size	.L.str.9, 103

	.type	.L.str.10,@object               # @.str.10
.L.str.10:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_5,AUTOGENERATED:T,ID:28,CHECKSUM:B52A"
	.size	.L.str.10, 114

	.type	.L.str.11,@object               # @.str.11
.L.str.11:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_5,AUTOGENERATED:T,ID:29,CHECKSUM:E093"
	.size	.L.str.11, 103

	.type	.L.str.12,@object               # @.str.12
.L.str.12:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_6,AUTOGENERATED:T,ID:2a,CHECKSUM:6ABE"
	.size	.L.str.12, 114

	.type	.L.str.13,@object               # @.str.13
.L.str.13:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_6,AUTOGENERATED:T,ID:2b,CHECKSUM:FE86"
	.size	.L.str.13, 103

	.type	.L.str.14,@object               # @.str.14
.L.str.14:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_7,AUTOGENERATED:T,ID:2c,CHECKSUM:37F2"
	.size	.L.str.14, 114

	.type	.L.str.15,@object               # @.str.15
.L.str.15:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_7,AUTOGENERATED:T,ID:2d,CHECKSUM:60CB"
	.size	.L.str.15, 103

	.type	.L.str.16,@object               # @.str.16
.L.str.16:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_8,AUTOGENERATED:T,ID:2e,CHECKSUM:8573"
	.size	.L.str.16, 114

	.type	.L.str.17,@object               # @.str.17
.L.str.17:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_8,AUTOGENERATED:T,ID:2f,CHECKSUM:114B"
	.size	.L.str.17, 103

	.type	.L.str.18,@object               # @.str.18
.L.str.18:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:0,functionName:FF_function_9,AUTOGENERATED:T,ID:30,CHECKSUM:B67F"
	.size	.L.str.18, 114

	.type	.L.str.19,@object               # @.str.19
.L.str.19:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_9,AUTOGENERATED:T,ID:31,CHECKSUM:E3C6"
	.size	.L.str.19, 103

	.type	.L.str.20,@object               # @.str.20
.L.str.20:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_11,AUTOGENERATED:T,ID:32,CHECKSUM:911F"
	.size	.L.str.20, 115

	.type	.L.str.21,@object               # @.str.21
.L.str.21:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_11,AUTOGENERATED:T,ID:33,CHECKSUM:FE97"
	.size	.L.str.21, 104

	.type	.L.str.22,@object               # @.str.22
.L.str.22:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_13,AUTOGENERATED:T,ID:34,CHECKSUM:EA06"
	.size	.L.str.22, 115

	.type	.L.str.23,@object               # @.str.23
.L.str.23:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_13,AUTOGENERATED:T,ID:35,CHECKSUM:858E"
	.size	.L.str.23, 104

	.type	.L.str.24,@object               # @.str.24
.L.str.24:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_15,AUTOGENERATED:T,ID:36,CHECKSUM:A12C"
	.size	.L.str.24, 115

	.type	.L.str.25,@object               # @.str.25
.L.str.25:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_15,AUTOGENERATED:T,ID:37,CHECKSUM:CEA4"
	.size	.L.str.25, 104

	.type	.L.str.26,@object               # @.str.26
.L.str.26:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_23,AUTOGENERATED:T,ID:38,CHECKSUM:10E2"
	.size	.L.str.26, 115

	.type	.L.str.27,@object               # @.str.27
.L.str.27:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_23,AUTOGENERATED:T,ID:39,CHECKSUM:7F6A"
	.size	.L.str.27, 104

	.type	.L.str.28,@object               # @.str.28
.L.str.28:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_25,AUTOGENERATED:T,ID:3a,CHECKSUM:A089"
	.size	.L.str.28, 115

	.type	.L.str.29,@object               # @.str.29
.L.str.29:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_25,AUTOGENERATED:T,ID:3b,CHECKSUM:0E80"
	.size	.L.str.29, 104

	.type	.L.str.30,@object               # @.str.30
.L.str.30:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_36,AUTOGENERATED:T,ID:3c,CHECKSUM:1101"
	.size	.L.str.30, 115

	.type	.L.str.31,@object               # @.str.31
.L.str.31:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_36,AUTOGENERATED:T,ID:3d,CHECKSUM:7C09"
	.size	.L.str.31, 104

	.type	.L.str.32,@object               # @.str.32
.L.str.32:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_42,AUTOGENERATED:T,ID:3e,CHECKSUM:CA25"
	.size	.L.str.32, 115

	.type	.L.str.33,@object               # @.str.33
.L.str.33:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_42,AUTOGENERATED:T,ID:3f,CHECKSUM:642C"
	.size	.L.str.33, 104

	.type	.L.str.34,@object               # @.str.34
.L.str.34:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_45,AUTOGENERATED:T,ID:40,CHECKSUM:D381"
	.size	.L.str.34, 115

	.type	.L.str.35,@object               # @.str.35
.L.str.35:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_45,AUTOGENERATED:T,ID:41,CHECKSUM:BC09"
	.size	.L.str.35, 104

	.type	.L.str.36,@object               # @.str.36
.L.str.36:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_49,AUTOGENERATED:T,ID:42,CHECKSUM:4755"
	.size	.L.str.36, 115

	.type	.L.str.37,@object               # @.str.37
.L.str.37:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_49,AUTOGENERATED:T,ID:43,CHECKSUM:28DD"
	.size	.L.str.37, 104

	.type	.L.str.38,@object               # @.str.38
.L.str.38:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_54,AUTOGENERATED:T,ID:44,CHECKSUM:1910"
	.size	.L.str.38, 115

	.type	.L.str.39,@object               # @.str.39
.L.str.39:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_54,AUTOGENERATED:T,ID:45,CHECKSUM:7698"
	.size	.L.str.39, 104

	.type	.L.str.40,@object               # @.str.40
.L.str.40:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_56,AUTOGENERATED:T,ID:46,CHECKSUM:A108"
	.size	.L.str.40, 115

	.type	.L.str.41,@object               # @.str.41
.L.str.41:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_55,AUTOGENERATED:T,ID:48,CHECKSUM:80DD"
	.size	.L.str.41, 115

	.type	.L.str.42,@object               # @.str.42
.L.str.42:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_55,AUTOGENERATED:T,ID:49,CHECKSUM:EF55"
	.size	.L.str.42, 104

	.type	.L.str.43,@object               # @.str.43
.L.str.43:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_57,AUTOGENERATED:T,ID:4a,CHECKSUM:C384"
	.size	.L.str.43, 115

	.type	.L.str.44,@object               # @.str.44
.L.str.44:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_58,AUTOGENERATED:T,ID:4c,CHECKSUM:B204"
	.size	.L.str.44, 115

	.type	.L.str.45,@object               # @.str.45
.L.str.45:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_58,AUTOGENERATED:T,ID:4d,CHECKSUM:DF0C"
	.size	.L.str.45, 104

	.type	.L.str.46,@object               # @.str.46
.L.str.46:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_60,AUTOGENERATED:T,ID:4e,CHECKSUM:E907"
	.size	.L.str.46, 115

	.type	.L.str.47,@object               # @.str.47
.L.str.47:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_60,AUTOGENERATED:T,ID:4f,CHECKSUM:470E"
	.size	.L.str.47, 104

	.type	.L.str.48,@object               # @.str.48
.L.str.48:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_61,AUTOGENERATED:T,ID:50,CHECKSUM:DA0B"
	.size	.L.str.48, 115

	.type	.L.str.49,@object               # @.str.49
.L.str.49:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_61,AUTOGENERATED:T,ID:51,CHECKSUM:B583"
	.size	.L.str.49, 104

	.type	.L.str.50,@object               # @.str.50
.L.str.50:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_62,AUTOGENERATED:T,ID:52,CHECKSUM:FEDE"
	.size	.L.str.50, 115

	.type	.L.str.51,@object               # @.str.51
.L.str.51:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_62,AUTOGENERATED:T,ID:53,CHECKSUM:9156"
	.size	.L.str.51, 104

	.type	.L.str.52,@object               # @.str.52
.L.str.52:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_63,AUTOGENERATED:T,ID:54,CHECKSUM:6093"
	.size	.L.str.52, 115

	.type	.L.str.53,@object               # @.str.53
.L.str.53:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_63,AUTOGENERATED:T,ID:55,CHECKSUM:0F1B"
	.size	.L.str.53, 104

	.type	.L.str.54,@object               # @.str.54
.L.str.54:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_64,AUTOGENERATED:T,ID:56,CHECKSUM:B774"
	.size	.L.str.54, 115

	.type	.L.str.55,@object               # @.str.55
.L.str.55:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_64,AUTOGENERATED:T,ID:57,CHECKSUM:D8FC"
	.size	.L.str.55, 104

	.type	.L.str.56,@object               # @.str.56
.L.str.56:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_65,AUTOGENERATED:T,ID:58,CHECKSUM:EF38"
	.size	.L.str.56, 115

	.type	.L.str.57,@object               # @.str.57
.L.str.57:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_65,AUTOGENERATED:T,ID:59,CHECKSUM:80B0"
	.size	.L.str.57, 104

	.type	.L.str.58,@object               # @.str.58
.L.str.58:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_66,AUTOGENERATED:T,ID:5a,CHECKSUM:30AC"
	.size	.L.str.58, 115

	.type	.L.str.59,@object               # @.str.59
.L.str.59:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_66,AUTOGENERATED:T,ID:5b,CHECKSUM:9EA5"
	.size	.L.str.59, 104

	.type	.L.str.60,@object               # @.str.60
.L.str.60:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_67,AUTOGENERATED:T,ID:5c,CHECKSUM:6DE0"
	.size	.L.str.60, 115

	.type	.L.str.61,@object               # @.str.61
.L.str.61:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_68,AUTOGENERATED:T,ID:5e,CHECKSUM:DF61"
	.size	.L.str.61, 115

	.type	.L.str.62,@object               # @.str.62
.L.str.62:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_68,AUTOGENERATED:T,ID:5f,CHECKSUM:7168"
	.size	.L.str.62, 104

	.type	.L.str.63,@object               # @.str.63
.L.str.63:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_69,AUTOGENERATED:T,ID:60,CHECKSUM:8C6C"
	.size	.L.str.63, 115

	.type	.L.str.64,@object               # @.str.64
.L.str.64:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_69,AUTOGENERATED:T,ID:61,CHECKSUM:E3E4"
	.size	.L.str.64, 104

	.type	.L.str.65,@object               # @.str.65
.L.str.65:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_70,AUTOGENERATED:T,ID:62,CHECKSUM:E21A"
	.size	.L.str.65, 115

	.type	.L.str.66,@object               # @.str.66
.L.str.66:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_70,AUTOGENERATED:T,ID:63,CHECKSUM:8D92"
	.size	.L.str.66, 104

	.type	.L.str.67,@object               # @.str.67
.L.str.67:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_71,AUTOGENERATED:T,ID:64,CHECKSUM:7C57"
	.size	.L.str.67, 115

	.type	.L.str.68,@object               # @.str.68
.L.str.68:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_71,AUTOGENERATED:T,ID:65,CHECKSUM:13DF"
	.size	.L.str.68, 104

	.type	.L.str.69,@object               # @.str.69
.L.str.69:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_72,AUTOGENERATED:T,ID:66,CHECKSUM:5882"
	.size	.L.str.69, 115

	.type	.L.str.70,@object               # @.str.70
.L.str.70:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_72,AUTOGENERATED:T,ID:67,CHECKSUM:370A"
	.size	.L.str.70, 104

	.type	.L.str.71,@object               # @.str.71
.L.str.71:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_74,AUTOGENERATED:T,ID:68,CHECKSUM:16A8"
	.size	.L.str.71, 115

	.type	.L.str.72,@object               # @.str.72
.L.str.72:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_73,AUTOGENERATED:T,ID:6a,CHECKSUM:3A0E"
	.size	.L.str.72, 115

	.type	.L.str.73,@object               # @.str.73
.L.str.73:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_73,AUTOGENERATED:T,ID:6b,CHECKSUM:9407"
	.size	.L.str.73, 104

	.type	.L.str.74,@object               # @.str.74
.L.str.74:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_76,AUTOGENERATED:T,ID:6c,CHECKSUM:9470"
	.size	.L.str.74, 115

	.type	.L.str.75,@object               # @.str.75
.L.str.75:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_75,AUTOGENERATED:T,ID:6e,CHECKSUM:73A4"
	.size	.L.str.75, 115

	.type	.L.str.76,@object               # @.str.76
.L.str.76:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_75,AUTOGENERATED:T,ID:6f,CHECKSUM:DDAD"
	.size	.L.str.76, 104

	.type	.L.str.77,@object               # @.str.77
.L.str.77:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_78,AUTOGENERATED:T,ID:70,CHECKSUM:15FD"
	.size	.L.str.77, 115

	.type	.L.str.78,@object               # @.str.78
.L.str.78:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_78,AUTOGENERATED:T,ID:71,CHECKSUM:7A75"
	.size	.L.str.78, 104

	.type	.L.str.79,@object               # @.str.79
.L.str.79:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_80,AUTOGENERATED:T,ID:72,CHECKSUM:B26A"
	.size	.L.str.79, 115

	.type	.L.str.80,@object               # @.str.80
.L.str.80:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_80,AUTOGENERATED:T,ID:73,CHECKSUM:DDE2"
	.size	.L.str.80, 104

	.type	.L.str.81,@object               # @.str.81
.L.str.81:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_81,AUTOGENERATED:T,ID:74,CHECKSUM:2C27"
	.size	.L.str.81, 115

	.type	.L.str.82,@object               # @.str.82
.L.str.82:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_81,AUTOGENERATED:T,ID:75,CHECKSUM:43AF"
	.size	.L.str.82, 104

	.type	.L.str.83,@object               # @.str.83
.L.str.83:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_82,AUTOGENERATED:T,ID:76,CHECKSUM:08F2"
	.size	.L.str.83, 115

	.type	.L.str.84,@object               # @.str.84
.L.str.84:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_82,AUTOGENERATED:T,ID:77,CHECKSUM:677A"
	.size	.L.str.84, 104

	.type	.L.str.85,@object               # @.str.85
.L.str.85:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_79,AUTOGENERATED:T,ID:78,CHECKSUM:4F31"
	.size	.L.str.85, 115

	.type	.L.str.86,@object               # @.str.86
.L.str.86:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_83,AUTOGENERATED:T,ID:7a,CHECKSUM:6A7E"
	.size	.L.str.86, 115

	.type	.L.str.87,@object               # @.str.87
.L.str.87:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_84,AUTOGENERATED:T,ID:7c,CHECKSUM:BD99"
	.size	.L.str.87, 115

	.type	.L.str.88,@object               # @.str.88
.L.str.88:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_84,AUTOGENERATED:T,ID:7d,CHECKSUM:D091"
	.size	.L.str.88, 104

	.type	.L.str.89,@object               # @.str.89
.L.str.89:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_86,AUTOGENERATED:T,ID:7e,CHECKSUM:C680"
	.size	.L.str.89, 115

	.type	.L.str.90,@object               # @.str.90
.L.str.90:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_86,AUTOGENERATED:T,ID:7f,CHECKSUM:6889"
	.size	.L.str.90, 104

	.type	.L.str.91,@object               # @.str.91
.L.str.91:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_52,AUTOGENERATED:T,ID:80,CHECKSUM:50BF"
	.size	.L.str.91, 115

	.type	.L.str.92,@object               # @.str.92
.L.str.92:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_52,AUTOGENERATED:T,ID:81,CHECKSUM:3F37"
	.size	.L.str.92, 104

	.type	.L.str.93,@object               # @.str.93
.L.str.93:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_51,AUTOGENERATED:T,ID:82,CHECKSUM:746A"
	.size	.L.str.93, 115

	.type	.L__const.FF_function_51.FF_x,@object # @__const.FF_function_51.FF_x
	.section	.rodata.cst16,"aM",@progbits,16
	.p2align	3
.L__const.FF_function_51.FF_x:
	.long	0                               # 0x0
	.long	0x3f000000                      # float 0.5
	.quad	0
	.size	.L__const.FF_function_51.FF_x, 16

	.type	.L.str.94,@object               # @.str.94
	.section	.rodata.str1.1,"aMS",@progbits,1
.L.str.94:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_51,AUTOGENERATED:T,ID:83,CHECKSUM:1BE2"
	.size	.L.str.94, 104

	.type	.L.str.95,@object               # @.str.95
.L.str.95:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_50,AUTOGENERATED:T,ID:84,CHECKSUM:EA27"
	.size	.L.str.95, 115

	.type	.L.str.96,@object               # @.str.96
.L.str.96:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_50,AUTOGENERATED:T,ID:85,CHECKSUM:85AF"
	.size	.L.str.96, 104

	.type	.L.str.97,@object               # @.str.97
.L.str.97:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_47,AUTOGENERATED:T,ID:86,CHECKSUM:A89D"
	.size	.L.str.97, 115

	.type	.L.str.98,@object               # @.str.98
.L.str.98:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_47,AUTOGENERATED:T,ID:87,CHECKSUM:C715"
	.size	.L.str.98, 104

	.type	.L.str.99,@object               # @.str.99
.L.str.99:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_46,AUTOGENERATED:T,ID:88,CHECKSUM:F0D1"
	.size	.L.str.99, 115

	.type	.L.str.100,@object              # @.str.100
.L.str.100:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_43,AUTOGENERATED:T,ID:8a,CHECKSUM:A5EE"
	.size	.L.str.100, 115

	.type	.L.str.101,@object              # @.str.101
.L.str.101:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_40,AUTOGENERATED:T,ID:8c,CHECKSUM:813B"
	.size	.L.str.101, 115

	.type	.L__const.FF_function_40.FF_x,@object # @__const.FF_function_40.FF_x
	.section	.rodata.cst16,"aM",@progbits,16
	.p2align	3
.L__const.FF_function_40.FF_x:
	.long	0                               # 0x0
	.long	0x3f000000                      # float 0.5
	.quad	0
	.size	.L__const.FF_function_40.FF_x, 16

	.type	.L.str.102,@object              # @.str.102
	.section	.rodata.str1.1,"aMS",@progbits,1
.L.str.102:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_40,AUTOGENERATED:T,ID:8d,CHECKSUM:EC33"
	.size	.L.str.102, 104

	.type	.L.str.103,@object              # @.str.103
.L.str.103:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_39,AUTOGENERATED:T,ID:8e,CHECKSUM:9387"
	.size	.L.str.103, 115

	.type	.L.str.104,@object              # @.str.104
.L.str.104:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_39,AUTOGENERATED:T,ID:8f,CHECKSUM:3D8E"
	.size	.L.str.104, 104

	.type	.L.str.105,@object              # @.str.105
.L.str.105:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_38,AUTOGENERATED:T,ID:90,CHECKSUM:A08B"
	.size	.L.str.105, 115

	.type	.L.str.106,@object              # @.str.106
.L.str.106:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_38,AUTOGENERATED:T,ID:91,CHECKSUM:CF03"
	.size	.L.str.106, 104

	.type	.L.str.107,@object              # @.str.107
.L.str.107:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_37,AUTOGENERATED:T,ID:92,CHECKSUM:D10B"
	.size	.L.str.107, 115

	.type	.L.str.108,@object              # @.str.108
.L.str.108:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_37,AUTOGENERATED:T,ID:93,CHECKSUM:BE83"
	.size	.L.str.108, 104

	.type	.L.str.109,@object              # @.str.109
.L.str.109:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_34,AUTOGENERATED:T,ID:94,CHECKSUM:36DF"
	.size	.L.str.109, 115

	.type	.L.str.110,@object              # @.str.110
.L.str.110:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_34,AUTOGENERATED:T,ID:95,CHECKSUM:5957"
	.size	.L.str.110, 104

	.type	.L.str.111,@object              # @.str.111
.L.str.111:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_33,AUTOGENERATED:T,ID:96,CHECKSUM:E138"
	.size	.L.str.111, 115

	.type	.L.str.112,@object              # @.str.112
.L.str.112:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_33,AUTOGENERATED:T,ID:97,CHECKSUM:8EB0"
	.size	.L.str.112, 104

	.type	.L.str.113,@object              # @.str.113
.L.str.113:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_32,AUTOGENERATED:T,ID:98,CHECKSUM:B974"
	.size	.L.str.113, 115

	.type	.L.str.114,@object              # @.str.114
.L.str.114:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_32,AUTOGENERATED:T,ID:99,CHECKSUM:D6FC"
	.size	.L.str.114, 104

	.type	.L.str.115,@object              # @.str.115
.L.str.115:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_31,AUTOGENERATED:T,ID:9a,CHECKSUM:66E0"
	.size	.L.str.115, 115

	.type	.L.str.116,@object              # @.str.116
.L.str.116:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_31,AUTOGENERATED:T,ID:9b,CHECKSUM:C8E9"
	.size	.L.str.116, 104

	.type	.L.str.117,@object              # @.str.117
.L.str.117:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_30,AUTOGENERATED:T,ID:9c,CHECKSUM:3BAC"
	.size	.L.str.117, 115

	.type	.L.str.118,@object              # @.str.118
.L.str.118:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_30,AUTOGENERATED:T,ID:9d,CHECKSUM:56A4"
	.size	.L.str.118, 104

	.type	.L.str.119,@object              # @.str.119
.L.str.119:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_29,AUTOGENERATED:T,ID:9e,CHECKSUM:96DB"
	.size	.L.str.119, 115

	.type	.L.str.120,@object              # @.str.120
.L.str.120:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_29,AUTOGENERATED:T,ID:9f,CHECKSUM:38D2"
	.size	.L.str.120, 104

	.type	.L.str.121,@object              # @.str.121
.L.str.121:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_28,AUTOGENERATED:T,ID:a0,CHECKSUM:F5ED"
	.size	.L.str.121, 115

	.type	.L.str.122,@object              # @.str.122
.L.str.122:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_27,AUTOGENERATED:T,ID:a2,CHECKSUM:846D"
	.size	.L.str.122, 115

	.type	.L.str.123,@object              # @.str.123
.L.str.123:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_26,AUTOGENERATED:T,ID:a4,CHECKSUM:1A20"
	.size	.L.str.123, 115

	.type	.L.str.124,@object              # @.str.124
.L.str.124:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_26,AUTOGENERATED:T,ID:a5,CHECKSUM:75A8"
	.size	.L.str.124, 104

	.type	.L.str.125,@object              # @.str.125
.L.str.125:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_21,AUTOGENERATED:T,ID:a6,CHECKSUM:CDC7"
	.size	.L.str.125, 115

	.type	.L.str.126,@object              # @.str.126
.L.str.126:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_21,AUTOGENERATED:T,ID:a7,CHECKSUM:A24F"
	.size	.L.str.126, 104

	.type	.L.str.127,@object              # @.str.127
.L.str.127:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_20,AUTOGENERATED:T,ID:a8,CHECKSUM:958B"
	.size	.L.str.127, 115

	.type	.L.str.128,@object              # @.str.128
.L.str.128:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_20,AUTOGENERATED:T,ID:a9,CHECKSUM:FA03"
	.size	.L.str.128, 104

	.type	.L.str.129,@object              # @.str.129
.L.str.129:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_19,AUTOGENERATED:T,ID:aa,CHECKSUM:6A05"
	.size	.L.str.129, 115

	.type	.L.str.130,@object              # @.str.130
.L.str.130:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_19,AUTOGENERATED:T,ID:ab,CHECKSUM:C40C"
	.size	.L.str.130, 104

	.type	.L.str.131,@object              # @.str.131
.L.str.131:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_18,AUTOGENERATED:T,ID:ac,CHECKSUM:3749"
	.size	.L.str.131, 115

	.type	.L.str.132,@object              # @.str.132
.L.str.132:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_18,AUTOGENERATED:T,ID:ad,CHECKSUM:5A41"
	.size	.L.str.132, 104

	.type	.L.str.133,@object              # @.str.133
.L.str.133:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_17,AUTOGENERATED:T,ID:ae,CHECKSUM:85C8"
	.size	.L.str.133, 115

	.type	.L.str.134,@object              # @.str.134
.L.str.134:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_17,AUTOGENERATED:T,ID:af,CHECKSUM:2BC1"
	.size	.L.str.134, 104

	.type	.L.str.135,@object              # @.str.135
.L.str.135:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:FF_function_16,AUTOGENERATED:T,ID:b0,CHECKSUM:D6C5"
	.size	.L.str.135, 115

	.type	.L__const.FF_function_16.FF_x,@object # @__const.FF_function_16.FF_x
	.section	.rodata.cst16,"aM",@progbits,16
	.p2align	3
.L__const.FF_function_16.FF_x:
	.long	0                               # 0x0
	.long	0x3f000000                      # float 0.5
	.quad	0
	.size	.L__const.FF_function_16.FF_x, 16

	.type	.L.str.136,@object              # @.str.136
	.section	.rodata.str1.1,"aMS",@progbits,1
.L.str.136:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:FF_function_16,AUTOGENERATED:T,ID:b1,CHECKSUM:B94D"
	.size	.L.str.136, 104

	.type	.L.str.137,@object              # @.str.137
.L.str.137:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:CF_function_87,AUTOGENERATED:T,ID:b2,CHECKSUM:9EF3"
	.size	.L.str.137, 115

	.type	.L.str.138,@object              # @.str.138
.L.str.138:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1071,PLID:-1,EBR:T,loopcom:DOWHILE,NESTLEV:0,UNR:NU,finitude:PFL,location:BEFORE,ID:10,CHECKSUM:7662"
	.size	.L.str.138, 148

	.type	.L.str.139,@object              # @.str.139
.L.str.139:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1071,location:BODY,ID:11,CHECKSUM:0060"
	.size	.L.str.139, 86

	.type	.L.str.140,@object              # @.str.140
.L.str.140:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1071,location:UNDEFINED,ID:12,DUMMY:T,CHECKSUM:69E0"
	.size	.L.str.140, 99

	.type	.L.str.141,@object              # @.str.141
.L.str.141:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1072,PLID:1071,loopcom:FOR,NESTLEV:1,UNR:NU,finitude:PFL,location:BEFORE,ID:13,EXR:T,CHECKSUM:D44D"
	.size	.L.str.141, 146

	.type	.L.str.142,@object              # @.str.142
.L.str.142:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1072,location:BODY,ID:14,CHECKSUM:3093"
	.size	.L.str.142, 86

	.type	.L.str.143,@object              # @.str.143
.L.str.143:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1072,location:UNDEFINED,ID:15,DUMMY:T,CHECKSUM:2EC6"
	.size	.L.str.143, 99

	.type	.L.str.144,@object              # @.str.144
.L.str.144:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1072,location:UNDEFINED,ID:16,DUMMY:T,CHECKSUM:DED2"
	.size	.L.str.144, 99

	.type	.L.str.145,@object              # @.str.145
.L.str.145:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1072,location:AFTER,ID:17,CHECKSUM:C0F2"
	.size	.L.str.145, 87

	.type	.L.str.146,@object              # @.str.146
.L.str.146:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1072,location:UNDEFINED,ID:18,DUMMY:T,CHECKSUM:BE9E"
	.size	.L.str.146, 99

	.type	.L.str.147,@object              # @.str.147
.L.str.147:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1071,location:UNDEFINED,ID:19,DUMMY:T,CHECKSUM:5993"
	.size	.L.str.147, 99

	.type	.L.str.148,@object              # @.str.148
.L.str.148:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1071,location:AFTER,ID:1a,CHECKSUM:EB01"
	.size	.L.str.148, 87

	.type	.L.str.149,@object              # @.str.149
.L.str.149:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1071,location:UNDEFINED,ID:1b,DUMMY:T,CHECKSUM:6A1C"
	.size	.L.str.149, 99

	.type	.L.str.150,@object              # @.str.150
.L.str.150:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:CF_function_87,AUTOGENERATED:T,ID:b3,CHECKSUM:F17B"
	.size	.L.str.150, 104

	.type	.L.str.151,@object              # @.str.151
.L.str.151:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>callable:1,functionName:main,AUTOGENERATED:T,ID:b4,CHECKSUM:953D"
	.size	.L.str.151, 105

	.type	.L.str.152,@object              # @.str.152
.L.str.152:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8MD>>ID:1,version:1.0.0,CHECKSUM:410F"
	.size	.L.str.152, 73

	.type	.L.str.153,@object              # @.str.153
.L.str.153:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>ID:2,CHECKSUM:45D1"
	.size	.L.str.153, 59

	.type	.L.str.154,@object              # @.str.154
.L.str.154:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>ID:3,CHECKSUM:8510"
	.size	.L.str.154, 59

	.type	.L.str.155,@object              # @.str.155
.L.str.155:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>ID:4,CHECKSUM:4751"
	.size	.L.str.155, 59

	.type	.L.str.156,@object              # @.str.156
.L.str.156:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>ID:5,CHECKSUM:8790"
	.size	.L.str.156, 59

	.type	.L.str.157,@object              # @.str.157
.L.str.157:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>ID:6,CHECKSUM:86D0"
	.size	.L.str.157, 59

	.type	.L.str.158,@object              # @.str.158
.L.str.158:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LVN:CF_LV1068,loopcom:DOWHILE,ICC:T,LVT:INT,LOOPID:1068,INEXP:0,UPEXP:+=13,PLID:-1,NESTLEV:0,TSEXP:!=2494,UNR:NU,finitude:PFL,EEX:T,location:BEFORE,ID:7,EXR:T,EGA:T,CHECKSUM:9596"
	.size	.L.str.158, 219

	.type	.L.str.159,@object              # @.str.159
.L.str.159:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1068,location:BODY,ID:8,__DECIMAL_FIELD__:%d,CHECKSUM:BB92"
	.size	.L.str.159, 106

	.type	.L.str.160,@object              # @.str.160
.L.str.160:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1068,location:UNDEFINED,ID:9,DUMMY:T,CHECKSUM:D301"
	.size	.L.str.160, 98

	.type	.L.str.161,@object              # @.str.161
.L.str.161:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1068,location:UNDEFINED,ID:a,DUMMY:T,CHECKSUM:109A"
	.size	.L.str.161, 98

	.type	.L.str.162,@object              # @.str.162
.L.str.162:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1068,location:AFTER,ID:b,CHECKSUM:680C"
	.size	.L.str.162, 86

	.type	.L.str.163,@object              # @.str.163
.L.str.163:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>LOOPID:1068,location:UNDEFINED,ID:c,DUMMY:T,CHECKSUM:7083"
	.size	.L.str.163, 98

	.type	.L.str.164,@object              # @.str.164
.L.str.164:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>ID:d,CHECKSUM:7B51"
	.size	.L.str.164, 59

	.type	.L.str.165,@object              # @.str.165
.L.str.165:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>ID:e,CHECKSUM:BB90"
	.size	.L.str.165, 59

	.type	.L.str.166,@object              # @.str.166
.L.str.166:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8CF>>ID:f,CHECKSUM:BAD0"
	.size	.L.str.166, 59

	.type	.L.str.167,@object              # @.str.167
.L.str.167:
	.asciz	"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8FF>>functionName:main,AUTOGENERATED:T,ID:b5,CHECKSUM:2C78"
	.size	.L.str.167, 94

	.ident	"Ubuntu clang version 14.0.0-1ubuntu1.1"
	.ident	"Ubuntu clang version 14.0.0-1ubuntu1.1"
	.section	".note.GNU-stack","",@progbits
