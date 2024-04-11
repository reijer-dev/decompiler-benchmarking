struct S {
    int i;
    float f;
};

void dosomething1() {
    int i = 10;
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:1,category:struct,CHECKSUM:5C9B", i);

    int name;
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:2,category:struct,CHECKSUM:5C9B", name);

    int i = 10;
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:3,category:struct,CHECKSUM:5C9B", i);

    int k1, k2, k3 = 10, k4, k5;
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:4,category:struct,CHECKSUM:5C9B", k3);

    int l1, l2=16, l3 = 17, l4, l5;
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:5,category:struct,CHECKSUM:5C9B", l3);

    unsigned int u1, u2;
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:6,category:struct,CHECKSUM:5C9B", u2);

    const unsigned int cu = 100;
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:7,category:struct,CHECKSUM:5C9B", cu);

    const unsigned cu2 = 100;
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:8,category:struct,CHECKSUM:5C9B", cu2);
}

void dosomething2(int64_t p1) {
    S* s = (S*)p1;
    s->i = 10;
    s->f = 0.5;
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:9,category:struct,CHECKSUM:5C9B", s);
}


void test_address_expressions() {
    //
    const int unsigned cu2 = 100;
    unsigned a;
    int b = 2;
    int* ptr;

    //constante
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:10,category:struct,CHECKSUM:5C9B", 0);

    //cast
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:11,category:struct,CHECKSUM:5C9B", (void*)cu2);

    //berekening
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:12,category:struct,CHECKSUM:5C9B", a + b);

    //berekening, echter er wordt maar 1 variabele gebruikt
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:13,category:struct,CHECKSUM:5C9B", a + 10);

    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:14,category:struct,CHECKSUM:5C9B", ptr);
}

void test_scopes() {
    unsigned i = 100;
    //now i is an unsigned int
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:15,category:struct,CHECKSUM:5C9B", i);

    for (int i=0; i<5; i++) {
        // now i is an int
        DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:16,category:struct,CHECKSUM:5C9B", i);

        int* i = 0;
        // now i is a pointer
        DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:17,category:struct,CHECKSUM:5C9B", i);

        {
            unsigned i = 15;
            DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:18,category:struct,CHECKSUM:5C9B", i);
        }

        // now i is a pointer again
        DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:19,category:struct,CHECKSUM:5C9B", i);
    }
}

void test_parameterscope(struct S* ptr) {
    DataStructureCodeMarker("c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,variable:ptr,ID:20,category:struct,CHECKSUM:5C9B", ptr);
}

void __cdecl __set_app_type(int param_1)

{
                    // WARNING: Could not recover jumptable at 0x000140009218. Too many branches
                    // WARNING: Treating indirect jump as call
  __set_app_type(param_1);
  return;
}

SIZE_T __stdcall VirtualQuery(LPCVOID lpAddress,PMEMORY_BASIC_INFORMATION lpBuffer,SIZE_T dwLength)

{
  SIZE_T SVar1;

                    // WARNING: Could not recover jumptable at 0x0001400092f8. Too many branches
                    // WARNING: Treating indirect jump as call
  SVar1 = VirtualQuery(lpAddress,lpBuffer,dwLength);
  return SVar1;
}

L0000000140007826(
    _unknown_ __rax,                       // r53
    intOrPtr __rcx,                        // r55
    intOrPtr _a8                           // _cfa_8
)
{
    _unknown_ _t2;                         // _t2

    __rcx = __rcx;
    __rax = __rax;
    _a8 = __rcx;
    //asm("bsr eax, [rbp+0x10]"); //todo the CParser can't handle this. why? happens only with "asm"
}

void* __fastcall _write_memory(void *a1, const void *a2, size_t a3)
{
  void *result; // rax

  if ( a3 )
  {
    mark_section_writable(a1);
    return memcpy(a1, a2, a3);
  }
  return result;
}

//from reko, edited because the code is not valid C
struct Eq_422 * myprintf(struct Eq_422 * rcx, struct Eq_422 * rdx, word64 r8, word64 r9, ptr64 r8Out, ptr64 r9Out)
{
	struct Eq_422 * rdx_62;
	return rdx_62;
}