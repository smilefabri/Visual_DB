import {FC, useEffect, useState} from "react";
import axios from "axios";

interface Record {
    username: string;
    email: string;
    num_database: number;
    num_table: number;
    isOnline: boolean;
}

export const AdminPage: FC = () => {
    const [usersList, setUsersList] = useState([]);

    useEffect(() => {
        // Effettua la tua chiamata API e aggiorna lo stato
        const HandlePrintListDb = async () => {
            try {
                const jwtoken = "Bearer " + "$" + sessionStorage.getItem("jwtToken");
                // Effettua la tua autenticazione qui, ad esempio con una chiamata fetch
                const response = await axios.get(
                    "http://localhost:8080/visualdb-api/admin/infoUser",
                    {
                        headers: {
                            Authorization: jwtoken, // Sostituisci con il tuo token effettivo
                            "Content-Type": "application/json",
                        },
                    }
                );
                if (response.status) {
                    console.log(response.data);
                    setUsersList(response.data)
                    console.log(response);
                } else {
                    console.error(JSON.stringify(response, null, 4));
                }
            } catch (error) {
                if (axios.isAxiosError(error)) {
                    console.log("error message: ", error.message);
                    return error.message;
                } else {
                    console.log("unexpected error: ", error);
                    return "An unexpected error occurred";
                }
            }
        };
        HandlePrintListDb();
    }, []);

    return (
        <>
            <h1 className="font-extrabold text-3xl text-center text-blue-900 uppercase mb-3 p-7">Admin Page</h1>
            <div className="flex h-full">
                {/* Sinistra: Tabella */}
                <div
                    className="flex-1 p-4 text-sm text-left rtl:text-right text-gray-500 dark:text-gray-400 rounded-tl">
                    {/* Tabella */}
                    <table
                        className="text-xs text-gray-700 bg-gray-50 dark:bg-gray-700 dark:text-gray-400 table-auto w-full">
                        <thead>
                        <tr>
                            <th className="px-4 py-2 uppercase">Stato</th>
                            <th className="px-4 py-2 uppercase">Username</th>
                            <th className="px-4 py-2 uppercase">E-Mail</th>
                            <th className="px-4 py-2 uppercase">Database</th>
                            <th className="px-4 py-2 uppercase">Table</th>
                        </tr>
                        </thead>
                        <tbody>
                        {/* Aggiungi qui le righe della tua tabella */}
                        {usersList.map((item: Record, index) => (
                            <tr
                                key={index}
                                className="odd:bg-white odd:dark:bg-gray-900 even:bg-gray-50 even:dark:bg-gray-800 border-b dark:border-gray-700"
                            >
                                {item.isOnline ? (
                                    <td className="px-6 py-4">
                                        <button type="button"
                                                className="text-white bg-green-700 hover:bg-green-800 focus:outline-none focus:ring-4 focus:ring-green-300 font-medium rounded-full text-sm p-1 text-center me-2 mb-2 dark:bg-green-600 dark:hover:bg-green-700 dark:focus:ring-green-800">
                                        </button>
                                    </td>
                                ) : (
                                    <td className="px-6 py-4">
                                        <button type="button"
                                                className="text-white bg-red-700 hover:bg-red-800 focus:outline-none focus:ring-4 focus:ring-red-300 font-medium rounded-full text-sm p-1 text-center me-2 mb-2 dark:bg-red-600 dark:hover:bg-red-700 dark:focus:ring-red-900">
                                        </button>
                                    </td>
                                )}
                                <td className="px-6 py-4">{item.username}</td>
                                <td className="px-6 py-4">{item.email}</td>
                                <td className="px-6 py-4">{item.num_database}</td>
                                <td className="px-6 py-4">{item.num_table}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                {/* Destra: 3 Box */}
                <div className="flex-1 p-4">
                    {/* Box 1 */}
                    <div className="bg-gray-800 p-6 mb-4 rounded-md shadow-md">
                        {/* Contenuto del Box 1 */}
                        <h3 className="font-extrabold text-3xl text-center text-blue-500 uppercase mb-3">
                            Utenti:
                        </h3>
                        <h5 className="font-semibold text-gray-100 text-2xl text-center">
                            {usersList.length}
                        </h5>
                    </div>

                    {/* Box 2 */}
                    <div className="bg-gray-800 p-6 mb-4 rounded-md shadow-md">
                        {/* Contenuto del Box 2 */}
                        <h3 className="font-extrabold text-3xl text-center text-blue-500 uppercase mb-3">
                            Database:
                        </h3>
                        <h5 className="font-semibold text-gray-100 text-2xl text-center">
                            {usersList.reduce((acc, record: Record) => acc + record.num_database, 0)}
                        </h5>
                    </div>

                    {/* Box 3 */}
                    <div className="bg-gray-800 p-6 mb-4 rounded-md shadow-md">
                        {/* Contenuto del Box 3 */}
                        <h3 className="font-extrabold text-3xl text-center text-blue-500 uppercase mb-3">
                            Tabelle:
                        </h3>
                        <h5 className="font-semibold text-gray-100 text-2xl text-center">
                            {usersList.reduce((acc, record: Record) => acc + record.num_table, 0)}
                        </h5>
                    </div>
                </div>
            </div>
        </>
    )
}